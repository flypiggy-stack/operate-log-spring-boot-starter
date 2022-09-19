package org.flypiggy.operate.log.spring.boot.starter.advice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.flypiggy.operate.log.spring.boot.starter.context.LogOperatorContext;
import org.flypiggy.operate.log.spring.boot.starter.datasource.DatasourceApi;
import org.flypiggy.operate.log.spring.boot.starter.exception.OperateLogException;
import org.flypiggy.operate.log.spring.boot.starter.model.Log;
import org.flypiggy.operate.log.spring.boot.starter.properties.ClassInfoEnum;
import org.flypiggy.operate.log.spring.boot.starter.properties.Exclude;
import org.flypiggy.operate.log.spring.boot.starter.properties.OperateLog;
import org.springframework.http.HttpMethod;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core logic code.
 */
@Slf4j
public class WebLogAdvice implements MethodInterceptor {
    private static final ObjectMapper objectMapper;

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
     * Whether to print the warning log during execution.
     */
    private final boolean waningLog;

    static {
        objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.registerModules(new ParameterNamesModule(), new Jdk8Module(), new JavaTimeModule());
    }

    public WebLogAdvice(DatasourceApi datasourceApi, OperateLog operateLog) {
        this.datasourceApi = datasourceApi;
        waningLog = operateLog.getWaningLog();
        classInfoEnum = operateLog.getClassInfoValue();
        classInfoIsTags = operateLog.getClassInfoValue().equals(ClassInfoEnum.TAGS);
        checkExclude(operateLog);
    }

    /**
     * Gets the method value of the annotation.
     *
     * @param annotation   annotation object
     * @param method       annotation method
     * @param defaultValue default value
     * @return {@link String} Annotation method return value.
     */
    private static String getAnnotationValue(Annotation annotation, String method, String defaultValue) {
        try {
            if (Objects.isNull(annotation)) return defaultValue;
            Method api = annotation.getClass().getDeclaredMethod(Objects.isNull(method) || method.trim().isEmpty() ? "value" : method);
            if (Objects.isNull(api)) return defaultValue;
            Object obj = api.invoke(annotation);
            if (obj.getClass().isArray()) return objectMapper.writeValueAsString(obj);
            return obj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    /**
     * Set support? and *(wildcards) Matches.
     *
     * @param s string to be matched
     * @param p matching rule
     * @return {@link Boolean} true is match; false isn`t match.
     */
    private static Boolean isMatch(String s, String p) {
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
    private static String getIp(HttpServletRequest request) {
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

    private String getClassInfo(Object o) {
        String classInfo;
        Class<?> targetClass = o.getClass();
        classInfo = getAnnotationValue(targetClass.getAnnotation(io.swagger.annotations.Api.class), classInfoEnum.name().toLowerCase(), targetClass.getSimpleName());
        if (classInfoIsTags) {
            JavaType javaType = objectMapper.getTypeFactory().constructParametricType(List.class, String.class);
            if (classInfo.contains("[") && classInfo.contains("\"")) {
                try {
                    List<String> list = objectMapper.readValue(classInfo, javaType);
                    if (!Objects.isNull(list) && !Objects.equals("", list.get(0))) {
                        classInfo = list.get(0);
                    }
                } catch (Exception e) {
                    throw new OperateLogException("Error get class info!");
                }
            }
        }
        return classInfo;
    }

    private void insert(Log logVo) {
        if (log.isDebugEnabled()) {
            log.debug("{}", logVo);
        }
        datasourceApi.save(logVo);
    }

    /**
     * Core code logic method.
     */
    @Override
    public Object invoke(MethodInvocation invocation) {
        try {
            if (waningLog && Objects.isNull(RequestContextHolder.getRequestAttributes())) {
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
            try {
                Object result = invocation.proceed();
                log.setSuccess(true);
                log.setResponseBody(Objects.isNull(result) ? null : (result.toString().length() > 1024 ? result.toString().substring(0, 1024) : result.toString()));
                return result;
            } catch (Throwable throwable) {
                StackTraceElement[] stackTraceElements = throwable.getStackTrace();
                StackTraceElement traceElement = stackTraceElements[0];
                String errorMessage = String.format("%s\n%s", traceElement, throwable);
                log.setSuccess(false);
                log.setErrorMessage(errorMessage);
            } finally {
                insert(log);
            }
        } catch (Throwable e) {
            if (waningLog) {
                log.warn("OPERATE-LOG Please report the error message, we will optimize the code after receiving it.");
            }
        }
        return null;
    }

    /**
     * Check whether the api interface is excluded.
     *
     * @param uri    uri
     * @param method http method
     * @return {@link Boolean} true need exclude; false don't need exclude.
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
        log.setMethodInfo(getAnnotationValue(method.getAnnotation(io.swagger.annotations.ApiOperation.class), "value", method.getName()));
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
}
