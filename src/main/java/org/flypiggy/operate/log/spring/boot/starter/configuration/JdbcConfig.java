package org.flypiggy.operate.log.spring.boot.starter.configuration;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flypiggy.operate.log.spring.boot.starter.exception.OperateLogException;
import org.flypiggy.operate.log.spring.boot.starter.properties.Jdbc;
import org.flypiggy.operate.log.spring.boot.starter.utils.JdbcUrlUtils;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@AllArgsConstructor
public class JdbcConfig {
    private final DataSourceProperties dataSourceProperties;

    public JdbcTemplate getJdbcTemplate() throws OperateLogException {
        DataSourceBuilder<?> dataSourceBuilder = dataSourceProperties.initializeDataSourceBuilder();
        log.info("OPERATE-LOG Create JDBC connection for operation log...");
        return new JdbcTemplate(dataSourceBuilder.build());
    }

    public void initCheck(JdbcTemplate jdbcTemplate, Jdbc jdbc) throws OperateLogException {
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
