package org.flypiggy.operate.log.spring.boot.starter.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flypiggy.operate.log.spring.boot.starter.exception.OperateLogException;
import org.flypiggy.operate.log.spring.boot.starter.properties.OperateLog;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.operate-log", name = "enable", havingValue = "true")
@AutoConfigureAfter(DataSourceProperties.class)
@Import({InitConfiguration.class, WebLogAdvisorConfiguration.class, OperateLog.class})
public class JdbcConfiguration {
    private final DataSourceProperties dataSourceProperties;

    @Bean("operateLogJdbcTemplate")
    @ConditionalOnBean(OperateLog.class)
    public JdbcTemplate operateLogJdbcTemplate() throws OperateLogException {
        DataSourceBuilder<?> dataSourceBuilder = dataSourceProperties.initializeDataSourceBuilder();
        log.info("OPERATE-LOG Create JDBC connection for operation log...");
        return new JdbcTemplate(dataSourceBuilder.build());
    }
}
