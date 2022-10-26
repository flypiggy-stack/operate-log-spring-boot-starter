package io.github.flypiggy.stack.operate.log.spring.boot.starter.configuration;

import io.github.flypiggy.stack.operate.log.spring.boot.starter.advice.WebLogAdvice;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.datasource.DatasourceApi;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.exception.OperateLogException;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.properties.DatasourceEnum;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.properties.Elasticsearch;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.properties.Jdbc;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.properties.OperateLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AdvisorBase {
    private final Logger log = LoggerFactory.getLogger(AdvisorBase.class);

    protected static final String expressionBase = "within(%s..*)";
    protected static final String annotationBase = "(%s || @annotation(io.github.flypiggy.stack.operate.log.spring.boot.starter.annotation.Log)) && !@annotation(io.github.flypiggy.stack.operate.log.spring.boot.starter.annotation.UnLog)";
    protected final OperateLog operateLog;

    public AdvisorBase(OperateLog operateLog) {
        this.operateLog = operateLog;
    }

    public AspectJExpressionPointcutAdvisor getPointcutAdvisor(DatasourceApi datasourceApi) {
        this.checkProperties();
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

    private void checkProperties() throws OperateLogException {
        String[] apiPackagePathArr = operateLog.getApiPackagePath();
        if (Objects.isNull(apiPackagePathArr) || apiPackagePathArr.length == 0) {
            throw new OperateLogException("Please check the package path in the configuration file! \n\tproperties: spring.operate-log.api-package-path");
        }
        if (null == operateLog.getStoreType()) {
            throw new OperateLogException("Please configure parameters 'spring.operate-log.store-type'!");
        }
        if (DatasourceEnum.JDBC.equals(operateLog.getStoreType())) {
            Jdbc jdbc = operateLog.getJdbc();
            if (Objects.isNull(jdbc)) {
                throw new OperateLogException("Please check the package path in the configuration file! \n\tproperties: spring.operate-log.jdbc");
            }
        } else if (DatasourceEnum.ELASTICSEARCH.equals(operateLog.getStoreType())) {
            Elasticsearch elasticsearch = operateLog.getElasticsearch();
            if (Objects.isNull(elasticsearch)) {
                throw new OperateLogException("Please check the package path in the configuration file! \n\tproperties: spring.operate-log.elasticsearch");
            }
            String indexName = elasticsearch.getIndex().getName();
            if (Objects.isNull(indexName) || "".equals(indexName)) {
                throw new OperateLogException("Please check the package path in the configuration file! \n\tproperties: spring.operate-log.elasticsearch.index-name");
            }
            if (!indexName.toLowerCase().equals(indexName)) {
                throw new OperateLogException("Please check configuration file properties \"spring.operate-log.elasticsearch.index-name\", don't use capital letters");
            }
        }
        // Check whether swagger dependency is introduced
        if (operateLog.getUseSwaggerAnnotation().equals(Boolean.TRUE)) {
            try {
                Class.forName("io.swagger.annotations.Api");
            } catch (ClassNotFoundException e) {
                throw new OperateLogException("If you need to mark log operation records with swagger annotations, you need to introduce swagger dependencies. If you do not need it, you need to configure it. \n\tproperties: spring.operate-log.use-swagger-annotation=false");
            }
        }
        log.info("OPERATE-LOG We will use {} as the data source for storing operation logs.", operateLog.getStoreType());
    }
}
