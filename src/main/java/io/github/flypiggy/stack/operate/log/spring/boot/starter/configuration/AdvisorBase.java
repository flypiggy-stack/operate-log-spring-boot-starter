package io.github.flypiggy.stack.operate.log.spring.boot.starter.configuration;

import io.github.flypiggy.stack.operate.log.spring.boot.starter.advice.WebLogAdvice;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.datasource.DatasourceApi;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.exception.OperateLogException;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.properties.OperateLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class AdvisorBase {
    protected static final String expressionBase = "within(%s..*)";
    protected static final String annotationBase = "(%s || @annotation(io.github.flypiggy.stack.operate.log.spring.boot.starter.annotation.Log)) && !@annotation(io.github.flypiggy.stack.operate.log.spring.boot.starter.annotation.UnLog)";
    protected final OperateLog operateLog;

    public AdvisorBase(OperateLog operateLog) {
        this.operateLog = operateLog;
    }

    public AspectJExpressionPointcutAdvisor getPointcutAdvisor(DatasourceApi datasourceApi) {
        AspectJExpressionPointcutAdvisor advisor = new AspectJExpressionPointcutAdvisor();
        advisor.setAdvice(new WebLogAdvice(datasourceApi, operateLog));
        String expressionClass = getExpression();
        String expression = String.format(annotationBase, expressionClass);
        log.info("OPERATE-LOG Scan package expression:{}", expression);
        advisor.setExpression(expression);
        return advisor;
    }

    private String getExpression() {
        String[] apiPackagePathArr = operateLog.getApiPackagePath();
        List<String> apiPackagePathList = Arrays.asList(apiPackagePathArr);
        if (apiPackagePathList.size() == 1) return String.format(expressionBase, apiPackagePathList.get(0));
        apiPackagePathList.sort(Comparator.comparingInt(String::length));
        List<String> repeat = apiPackagePathList.stream()
                .distinct()
                .filter(it -> apiPackagePathList.stream().anyMatch(init -> init.startsWith(it) && !it.equals(init)))
                .collect(Collectors.toList());
        if (!repeat.isEmpty()) {
            throw new OperateLogException("Error duplicate path! The package path requiring operation audit cannot be included in other paths!");
        }
        StringBuilder expression = new StringBuilder();
        for (String apiPackagePath : apiPackagePathList) {
            expression.insert(0, String.format(expressionBase, apiPackagePath) + " || ");
        }
        return expression.substring(0, expression.lastIndexOf(" || "));
    }
}
