package org.flypiggy.operate.log.spring.boot.starter.configuration;

import lombok.extern.slf4j.Slf4j;
import org.flypiggy.operate.log.spring.boot.starter.advice.WebLogAdvice;
import org.flypiggy.operate.log.spring.boot.starter.datasource.impl.ElasticsearchRepository;
import org.flypiggy.operate.log.spring.boot.starter.datasource.impl.JdbcRepository;
import org.flypiggy.operate.log.spring.boot.starter.exception.OperateLogException;
import org.flypiggy.operate.log.spring.boot.starter.properties.Elasticsearch;
import org.flypiggy.operate.log.spring.boot.starter.properties.Jdbc;
import org.flypiggy.operate.log.spring.boot.starter.properties.OperateLog;
import org.flypiggy.operate.log.spring.boot.starter.repository.LogRepository;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.flypiggy.operate.log.spring.boot.starter.properties.DatasourceEnum.ELASTICSEARCH;
import static org.flypiggy.operate.log.spring.boot.starter.properties.DatasourceEnum.JDBC;

@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "spring.operate-log", name = "enable", havingValue = "true")
@AutoConfigureAfter(OperateLog.class)
public class WebLogAdvisorConfiguration {
    private static final String expressionBase = "within(%s..*)";
    private final OperateLog operateLog;

    public WebLogAdvisorConfiguration(OperateLog operateLog) {
        this.operateLog = operateLog;
        this.checkProperties();
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.operate-log", name = "datasource-type", havingValue = "jdbc", matchIfMissing = true)
    public AspectJExpressionPointcutAdvisor jdbcConfigurableAdvisor(DataSourceProperties dataSourceProperties) {
        JdbcConfig jdbcConfig = new JdbcConfig(dataSourceProperties);
        JdbcTemplate jdbcTemplate = jdbcConfig.getJdbcTemplate();
        jdbcConfig.initCheck(jdbcTemplate, operateLog.getJdbc().getTableName());
        AspectJExpressionPointcutAdvisor advisor = getPointcutAdvisor();
        advisor.setAdvice(new WebLogAdvice(new JdbcRepository(jdbcTemplate), operateLog));
        return advisor;
    }


    @Bean
    @ConditionalOnProperty(prefix = "spring.operate-log", name = "datasource-type", havingValue = "elasticsearch")
    public AspectJExpressionPointcutAdvisor esConfigurableAdvisor(LogRepository logRepository) {
        AspectJExpressionPointcutAdvisor advisor = getPointcutAdvisor();
        advisor.setAdvice(new WebLogAdvice(new ElasticsearchRepository(logRepository), operateLog));
        return advisor;
    }

    private AspectJExpressionPointcutAdvisor getPointcutAdvisor() {
        AspectJExpressionPointcutAdvisor advisor = new AspectJExpressionPointcutAdvisor();
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

    private void checkProperties() throws OperateLogException {
        String[] apiPackagePathArr = operateLog.getApiPackagePath();
        if (Objects.isNull(apiPackagePathArr) || apiPackagePathArr.length == 0) {
            throw new OperateLogException("Please check the package path in the configuration file! \n\tproperties: spring.operate-log.api-package-path");
        }
        if (JDBC.equals(operateLog.getDatasourceType())) {
            Jdbc jdbc = operateLog.getJdbc();
            if (Objects.isNull(jdbc)) {
                throw new OperateLogException("Please check the package path in the configuration file! \n\tproperties: spring.operate-log.jdbc");
            }
        } else if (ELASTICSEARCH.equals(operateLog.getDatasourceType())) {
            Elasticsearch elasticsearch = operateLog.getElasticsearch();
            if (Objects.isNull(elasticsearch)) {
                throw new OperateLogException("Please check the package path in the configuration file! \n\tproperties: spring.operate-log.elasticsearch");
            }
            String indexName = elasticsearch.getIndexName();
            if (Objects.isNull(indexName) || "".equals(indexName)) {
                throw new OperateLogException("Please check the package path in the configuration file! \n\tproperties: spring.operate-log.elasticsearch.index-name");
            }
            if (!indexName.toLowerCase().equals(indexName)) {
                throw new OperateLogException("Please check configuration file properties \"spring.operate-log.elasticsearch.index-name\", don't use capital letters");
            }
        }
    }
}
