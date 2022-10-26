package io.github.flypiggy.stack.operate.log.spring.boot.starter.advice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.context.LogOperatorContext;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.datasource.DatasourceApi;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.exception.OperateLogException;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.model.Log;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.properties.ClassInfoEnum;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.properties.Exclude;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.properties.OperateLog;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.properties.PrintLogLevelEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.flypiggy.stack.operate.log.spring.boot.starter.properties.PrintLogLevelEnum.ERROR;
import static io.github.flypiggy.stack.operate.log.spring.boot.starter.properties.PrintLogLevelEnum.WARNING;

/**
 * Core logic code.
 */
public class WebLogAdvice implements MethodInterceptor {
    private final Logger log = LoggerFactory.getLogger(WebLogAdvice.class);
    private static final ObjectMapper objectMapper;
    private static final JavaType JAVA_TYPE;

    /**
     * Interface exclusion.
     * Set support? and *(wildcards) Matches.
     */
    private static final Map<String, Set<String>> excludeApiMap = new HashMap<>();
    /**
     * Exclude a type of request method interface.
     */
    private static Set<String> excludeHttpMethods = new HashSet<>();
    /**
     * Need to specify the exception thrown.
     */
    private final Set<String> thrownExceptionNameSet;
    /**
     * datasource: Inject the corresponding storage data source according to the configuration.
     */
    private final DatasourceApi datasourceApi;
    /**
     * see to {@link ClassInfoEnum}
     */
    private final ClassInfoEnum classInfoEnum;
    /**
     * Whether to be 'tags' of 'spring.operate-log.class-info-value'
     */
    private final boolean classInfoIsTags;

    static {
        objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.registerModules(new ParameterNamesModule(), new Jdk8Module(), new JavaTimeModule());
        JAVA_TYPE = objectMapper.getTypeFactory().constructParametricType(List.class, String.class);
    }

    /**
     * Whether to be null of 'spring.operate-log.exclude'
     */
    private boolean excludeIsNull = true;
    /**
     * Whether to be null of 'spring.operate-log.exclude.http-method'
     */
    private boolean excludeHttpMethodIsnull = true;
    /**
     * Whether to be null of 'spring.operate-log.exclude.api'
     */
    private boolean excludeApiIsnull = true;
    /**
     * Whether to be null of 'spring.operate-log.thrown-exception-name'
     */
    private final boolean thrownExceptionNameIsNull;

    /**
     * Whether to use the swagger annotation.
     */
    private final boolean useSwaggerAnnotation;

    /**
     * Whether to print the warning log during execution.
     */
    private final PrintLogLevelEnum printLogLevel;


    public WebLogAdvice(DatasourceApi datasourceApi, OperateLog operateLog) {
        this.datasourceApi = datasourceApi;
        this.classInfoEnum = operateLog.getClassInfoValue();
        this.printLogLevel = operateLog.getPrintLogLevel();
        this.useSwaggerAnnotation = operateLog.getUseSwaggerAnnotation();
        this.classInfoIsTags = this.useSwaggerAnnotation && operateLog.getClassInfoValue().equals(ClassInfoEnum.TAGS);
        checkExclude(operateLog);
        String[] thrownExceptionNameArr = operateLog.getThrownExceptionName();
        thrownExceptionNameSet = thrownExceptionNameArr == null
                ? Collections.emptySet()
                : Arrays.stream(thrownExceptionNameArr).collect(Collectors.toSet());
        this.thrownExceptionNameIsNull = thrownExceptionNameSet.isEmpty();
    }

    private void checkExclude(OperateLog operateLog) {
        Exclude exclude = operateLog.getExclude();
        if (Objects.isNull(operateLog.getExclude())) return;
        excludeIsNull = false;
        HttpMethod[] httpMethodArr = exclude.getHttpMethod();
        if (!Objects.isNull(httpMethodArr)) {
            excludeHttpMethodIsnull = false;
            excludeHttpMethods = Arrays.stream(httpMethodArr).map(Enum::name).collect(Collectors.toSet());
        }
        Map<HttpMethod, String[]> excludeMap = exclude.getApi();
        if (!Objects.isNull(excludeMap)) {
            excludeApiIsnull = false;
            excludeMap.forEach((k, v) -> excludeApiMap.put(k.name(), new HashSet<>(Arrays.asList(v))));
        }
    }

    /**
     * Use class of {@link LogOperatorContext}  to Get Operator
     */
    private String getOperator() {
        return LogOperatorContext.get();
    }

    /**
     * Core code logic method.
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();
        if (Objects.isNull(RequestContextHolder.getRequestAttributes())) {
            log.warn("OPERATE-LOG The method is not a web interface! " + "If you do not want to see this prompt, you need to reconfigurate 'spring.operate-log.api-package-path' to ensure that only web api methods are in these packages.");
        }
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        if (log.isDebugEnabled()) {
            log.debug("------------------------------------------------------------------------------");
            log.debug("Request source: {}", getIp(request));
            log.debug("Operator:       {}", getOperator());
            log.debug("Request method: {}", request.getMethod());
            log.debug("Api uri:        {}", request.getRequestURI());
            log.debug("Request now:    {}", LocalDateTime.now());
            log.debug("------------------------------------------------------------------------------");
        }
        if (isExcludeApi(request.getRequestURI(), request.getMethod())) {
            log.debug("Api exclude. Request method: {}, URI: {}", request.getRequestURI(), request.getMethod());
            return invocation.proceed();
        }
        Log log = getBaseLogObj(invocation, request);
        log.setTimeTaken(System.currentTimeMillis() - startTime);
        try {
            Object result = invocation.proceed();
            log.setSuccess(true);
            log.setResponseBody(Objects.isNull(result) ? null : result.toString());
            return result;
        } catch (Throwable throwable) {
            StackTraceElement[] stackTraceElements = throwable.getStackTrace();
            StackTraceElement traceElement = stackTraceElements[0];
            String errorMessage = String.format("%s\n%s", traceElement, throwable);
            log.setSuccess(false);
            log.setErrorMessage(errorMessage);
            if (thrownExceptionNameIsNull || thrownExceptionNameSet.contains(throwable.getClass().getSimpleName())) {
                throw throwable;
            }
        } finally {
            insert(log);
        }
        return null;
    }

    /**
     * Gets the method value of the annotation.
     *
     * @param annotation   annotation object
     * @param method       annotation method
     * @param defaultValue default value
     * @return {@link String} Annotation method return value.
     */
    private String getAnnotationValue(Annotation annotation, String method, String defaultValue) {
        try {
            if (Objects.isNull(annotation)) return defaultValue;
            Method api = annotation.getClass().getDeclaredMethod(Objects.isNull(method) || method.trim().isEmpty() ? "value" : method);
            if (Objects.isNull(api)) return defaultValue;
            Object obj = api.invoke(annotation);
            if (obj.getClass().isArray()) return objectMapper.writeValueAsString(obj);
            return obj.toString();
        } catch (Exception e) {
            log.warn("OPERATE-LOG swagger annotation exception occurred in generating class name or method name!");
            commonLogPrint(e);
        }
        return defaultValue;
    }

    /**
     * Check whether the api interface is excluded.
     *
     * @param uri    uri
     * @param method http method
     * @return {@link java.lang.Boolean} true need exclude; false don't need exclude.
     */
    private Boolean isExcludeApi(String uri, String method) {
        if (excludeIsNull) return false;
        if (!excludeHttpMethodIsnull) {
            if (excludeHttpMethods.contains(method)) {
                return true;
            }
        }
        if (excludeApiIsnull) return false;
        String methodKey = excludeApiMap.keySet().stream().filter(method::equals).findFirst().orElse(null);
        if (Objects.isNull(methodKey)) return false;
        return excludeApiMap.get(methodKey).stream().anyMatch(p -> isMatch(uri, p));
    }

    /**
     * Set support? and *(wildcards) Matches.
     *
     * @param s string to be matched
     * @param p matching rule
     * @return {@link Boolean} true is match; false isn`t match.
     */
    private Boolean isMatch(String s, String p) {
        // Index position of 's'
        int i = 0;
        // Index position of 'p'
        int j = 0;
        // Location of backtracking when wildcards
        int ii = -1;
        int jj = -1;
        while (i < s.length()) {
            if (j < p.length() && p.charAt(j) == '*') {
                // Encountered wildcard characters, record the position, rule string 1, and locate the non-wildcard string
                ii = i;
                jj = j;
                j++;
            } else if (j < p.length() && (s.charAt(i) == p.charAt(j) || p.charAt(j) == '?')) {
                // is match
                i++;
                j++;
            } else {
                // The match failed, and it is necessary to judge whether 's' is matched by 'p'(has '*'). If it is equal to '-1', there is no wildcard before it.
                if (jj == -1) return false;
                // Return to the position where the wildcard was previously recorded
                j = jj;
                // The matching string also returns to the recorded position and moves back by one bit
                i = ii + 1;
            }
        }
        // When each field of 's' is successfully matched, it is judged that the remaining string of 'p', if it is '*', it will be released.
        while (j < p.length() && p.charAt(j) == '*') j++;
        // The match is successful at the end of detection.
        return j == p.length();
    }

    /**
     * Get access to real IP.
     */
    private String getIp(HttpServletRequest request) {
        if (Objects.isNull(request)) {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            request = Objects.requireNonNull(attributes).getRequest();
        }
        String ipAddress = request.getHeader("x-forwarded-for");
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ipAddress != null && ipAddress.length() > 15) { //"***.***.***.***".length() = 15
            if (ipAddress.indexOf(",") > 0) {
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
            }
        }
        return ipAddress;
    }

    private void insert(Log logVo) {
        if (log.isDebugEnabled()) {
            log.debug("{}", logVo);
        }
        try {
            datasourceApi.save(logVo);
        } catch (Exception e) {
            this.commonLogPrint(e);
        }
    }

    /**
     * Build Log objects
     */
    private Log getBaseLogObj(MethodInvocation invocation, HttpServletRequest request) {
        Log log = new Log();
        log.setIp(getIp(request));
        log.setOperator(getOperator());
        log.setMethod(request.getMethod());
        log.setUri(request.getRequestURI());
        Object o = invocation.getThis();
        String classInfo = "";
        if (!Objects.isNull(o)) {
            classInfo = getClassInfo(o);
        }
        log.setClassInfo(classInfo);
        Method method = invocation.getMethod();
        method.setAccessible(true);
        log.setMethodInfo(getMethodInfo(method));
        Object[] arguments = invocation.getArguments();
        if (arguments.length == 1) {
            log.setRequestBody("[" + arguments[0] + "]");
        } else if (arguments.length >= 1) {
            StringBuilder requestStr = new StringBuilder();
            for (Object argument : arguments) {
                requestStr.append(argument).append(",");
            }
            requestStr = new StringBuilder("[" + requestStr.substring(0, requestStr.length() - 1) + "]");
            log.setRequestBody(requestStr.toString());
        }
        return log;
    }

    private String getClassInfo(Object o) {
        Class<?> targetClass = o.getClass();
        String classInfo = targetClass.getSimpleName();
        if (!useSwaggerAnnotation) return classInfo;
        try {
            Api annotation = targetClass.getAnnotation(Api.class);
            classInfo = getAnnotationValue(annotation, classInfoEnum.name().toLowerCase(), targetClass.getSimpleName());
        } catch (Throwable e) {
            exceptionLogPrint(e);
        }
        if (classInfoIsTags) {
            if (classInfo.contains("[") && classInfo.contains("\"")) {
                try {
                    List<String> list = objectMapper.readValue(classInfo, JAVA_TYPE);
                    if (!Objects.isNull(list) && !Objects.equals("", list.get(0))) {
                        classInfo = list.get(0);
                    }
                } catch (Exception e) {
                    commonLogPrint(e);
                }
            }
        }
        return Objects.isNull(classInfo) || classInfo.trim().isEmpty() || "[\"\"]".equals(classInfo) ? targetClass.getSimpleName() : classInfo;
    }

    private String getMethodInfo(Method method) {
        String methodInfo = method.getName();
        if (useSwaggerAnnotation) {
            try {
                methodInfo = getAnnotationValue(method.getAnnotation(ApiOperation.class), "value", method.getName());
            } catch (Throwable e) {
                exceptionLogPrint(e);
            }
            if (Objects.isNull(methodInfo) || methodInfo.trim().isEmpty()) {
                methodInfo = method.getName();
            }
        }
        return methodInfo;
    }

    private void exceptionLogPrint(Throwable e) {
        if (e instanceof NoClassDefFoundError) {
            throw new OperateLogException("To obtain swagger annotation exception, please check spring.operate-log.use-swagger-annotation configuration. if true is configured, you need to swagger related dependencies in!", e);
        } else {
            if (WARNING.equals(printLogLevel)) {
                log.warn("OPERATE-LOG Please report the error message, we will optimize the code after receiving it.");
            } else if (ERROR.equals(printLogLevel)) {
                log.error("OPERATE-LOG This exception will not affect your main program flow, but operation logging cannot be saved.", e);
            }
        }
    }

    private void commonLogPrint(Exception e) {
        if (WARNING.equals(printLogLevel)) {
            log.warn("OPERATE-LOG Please report the error message, we will optimize the code after receiving it.");
        } else if (ERROR.equals(printLogLevel)) {
            log.error("OPERATE-LOG This exception will not affect your main program flow, but operation logging cannot be saved.", e);
        }
    }
}
