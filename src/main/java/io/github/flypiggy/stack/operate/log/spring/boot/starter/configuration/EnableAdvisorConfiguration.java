package io.github.flypiggy.stack.operate.log.spring.boot.starter.configuration;

import io.github.flypiggy.stack.operate.log.spring.boot.starter.exception.OperateLogException;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.properties.DatasourceEnum;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.properties.Elasticsearch;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.properties.Jdbc;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.properties.OperateLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
@ConditionalOnProperty(prefix = "spring.operate-log", name = "enable", havingValue = "true")
@AutoConfigureAfter(OperateLog.class)
public class EnableAdvisorConfiguration {

    private final Logger log = LoggerFactory.getLogger(EnableAdvisorConfiguration.class);

    private final OperateLog operateLog;

    public EnableAdvisorConfiguration(OperateLog operateLog) {
        this.operateLog = operateLog;
        checkProperties();
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.operate-log", name = "store-type", havingValue = "mongodb")
    public void enableMongo() {
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.operate-log", name = "store-type", havingValue = "elasticsearch")
    public void enableEs() {
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.operate-log", name = "store-type", havingValue = "jdbc")
    public void enableJdbc() {
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
        log.info("OPERATE-LOG We will use {} as the data source for storing operation logs.", operateLog.getStoreType());
    }
}
