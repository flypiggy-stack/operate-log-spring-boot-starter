package org.flypiggy.operate.log.spring.boot.starter.configuration;

import lombok.extern.slf4j.Slf4j;
import org.flypiggy.operate.log.spring.boot.starter.advice.WebLogAdvice;
import org.flypiggy.operate.log.spring.boot.starter.datasource.DatasourceApi;
import org.flypiggy.operate.log.spring.boot.starter.exception.OperateLogException;
import org.flypiggy.operate.log.spring.boot.starter.properties.OperateLog;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class AdvisorBase {
    protected static final String expressionBase = "within(%s..*)";
    protected final OperateLog operateLog;

    public AdvisorBase(OperateLog operateLog) {
        this.operateLog = operateLog;
    }

    public AspectJExpressionPointcutAdvisor getPointcutAdvisor(DatasourceApi datasourceApi) {
        AspectJExpressionPointcutAdvisor advisor = new AspectJExpressionPointcutAdvisor();
        advisor.setAdvice(new WebLogAdvice(datasourceApi, operateLog));
        String expression = getExpression();
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
