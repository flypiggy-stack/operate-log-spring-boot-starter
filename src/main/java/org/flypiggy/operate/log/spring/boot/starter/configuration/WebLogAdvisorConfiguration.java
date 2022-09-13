package org.flypiggy.operate.log.spring.boot.starter.configuration;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flypiggy.operate.log.spring.boot.starter.advice.WebLogAdvice;
import org.flypiggy.operate.log.spring.boot.starter.exception.OperateLogException;
import org.flypiggy.operate.log.spring.boot.starter.properties.OperateLog;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Configuration
@ConditionalOnProperty(prefix = "spring.operate-log", name = "enable", havingValue = "true")
@AutoConfigureAfter({OperateLog.class, JdbcConfiguration.class})
public class WebLogAdvisorConfiguration {
    private static final String expressionBase = "within(%s..*)";
    private final OperateLog operateLog;
    private final JdbcTemplate operateLogJdbcTemplate;

    @Bean
    public AspectJExpressionPointcutAdvisor configurableAdvisor() {
        AspectJExpressionPointcutAdvisor advisor = new AspectJExpressionPointcutAdvisor();
        String expression = getExpression();
        log.info("OPERATE-LOG Scan package expression:{}", expression);
        advisor.setExpression(expression);
        advisor.setAdvice(new WebLogAdvice(operateLogJdbcTemplate, operateLog));
        return advisor;
    }

    private String getExpression() {
        String[] apiPackagePathArr = operateLog.getApiPackagePath();
        if (Objects.isNull(apiPackagePathArr) || apiPackagePathArr.length == 0) {
            throw new OperateLogException("Please configure the package path in the configuration file! \nproperties: spring.operate-log.api-package-path");
        }
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
