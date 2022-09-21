package io.github.flypiggy.stack.operate.log.spring.boot.starter.configuration.advisor;

import io.github.flypiggy.stack.operate.log.spring.boot.starter.configuration.AdvisorBase;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.configuration.EnableAdvisorConfiguration;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.datasource.impl.JdbcRepository;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.exception.OperateLogException;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.properties.Jdbc;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.properties.OperateLog;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.utils.JdbcUrlUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@Configuration
@ConditionalOnBean(name = "enableJdbc")
@AutoConfigureAfter(EnableAdvisorConfiguration.class)
public class JdbcAdvisorConfiguration extends AdvisorBase {

    private final DataSourceProperties dataSourceProperties;

    public JdbcAdvisorConfiguration(OperateLog operateLog, DataSourceProperties dataSourceProperties) {
        super(operateLog);
        this.dataSourceProperties = dataSourceProperties;
    }

    @Bean
    public AspectJExpressionPointcutAdvisor jdbcConfigurableAdvisor() {
        JdbcTemplate jdbcTemplate = this.getJdbcTemplate();
        Jdbc jdbc = operateLog.getJdbc();
        String tableName = jdbc.getTableName();
        this.initCheck(jdbcTemplate, jdbc);
        String insertSql = String.format(jdbc.getInsertSql(), tableName);
        return getPointcutAdvisor(new JdbcRepository(jdbcTemplate, insertSql));
    }

    private JdbcTemplate getJdbcTemplate() throws OperateLogException {
        DataSourceBuilder<?> dataSourceBuilder = dataSourceProperties.initializeDataSourceBuilder();
        log.info("OPERATE-LOG Create JDBC connection for operation log...");
        return new JdbcTemplate(dataSourceBuilder.build());
    }

    private void initCheck(JdbcTemplate jdbcTemplate, Jdbc jdbc) throws OperateLogException {
        String tableName = jdbc.getTableName();
        log.info("OPERATE-LOG Initialization checks whether the database exists. Check whether the table exists.");
        try {
            Integer result = jdbcTemplate.queryForObject(jdbc.getCheckDatabaseSql(), Integer.class);
            if (1 != result) {
                throw new OperateLogException();
            }
        } catch (DataAccessException e) {
            throw new OperateLogException("Error database not found! Please configure the correct database connection!");
        }
        // 检查表存在
        String databaseName = JdbcUrlUtils.findDatabaseName(dataSourceProperties.getUrl());
        String checkTableSql = jdbc.getCheckTableSql().contains("%s") ? String.format(jdbc.getCheckTableSql(), tableName, databaseName) : jdbc.getCheckTableSql();
        Long value = jdbcTemplate.queryForObject(checkTableSql, Long.class);
        if (value != null && value > 0) {
            log.info("OPERATE-LOG There is already an operation log table. There is no need to create a new table! Table's name is {}.", tableName);
            return;
        }
        log.info("OPERATE-LOG The operation log table does not exist yet. We are about to create a new table! Table's name is {}.", tableName);
        jdbcTemplate.update(String.format(jdbc.getCreateTableSql(), tableName));
    }
}
