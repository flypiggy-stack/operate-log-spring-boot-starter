package org.flypiggy.operate.log.spring.boot.starter.configuration;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flypiggy.operate.log.spring.boot.starter.exception.OperateLogException;
import org.flypiggy.operate.log.spring.boot.starter.utils.JdbcUrlUtils;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@AllArgsConstructor
public class JdbcConfig {
    private static final String createTableSql = "create table %s\n" +
            "(\n" +
            "    id                bigint unsigned primary key auto_increment not null comment 'primary key',\n" +
            "    ip                varchar(128)                               not null comment 'ip address',\n" +
            "    operator          varchar(256)                               null comment 'operator',\n" +
            "    method            varchar(20)                                not null comment 'request method',\n" +
            "    uri               varchar(256)                               not null comment 'request URI',\n" +
            "    class_info        varchar(256)                               not null comment 'class info',\n" +
            "    method_info       varchar(256)                               not null comment 'method info',\n" +
            "    success           tinyint(1)                                 not null comment '1:success 0:failure',\n" +
            "    request_body      text                                       null comment 'requestor',\n" +
            "    response_body     text                                       null comment 'responder',\n" +
            "    error_message     text                                       null comment 'error message',\n" +
            "    create_time       datetime  default CURRENT_TIMESTAMP        not null comment 'create time',\n" +
            "    update_time       timestamp default CURRENT_TIMESTAMP        not null on update CURRENT_TIMESTAMP comment 'update time'\n" +
            ")";
    private static final String checkTableSql = "select count(1) from information_schema.tables where table_name = ? and table_schema = ? limit 1";
    private static final String checkDatabaseSql = "select 1";
    private final DataSourceProperties dataSourceProperties;

    public JdbcTemplate getJdbcTemplate() throws OperateLogException {
        DataSourceBuilder<?> dataSourceBuilder = dataSourceProperties.initializeDataSourceBuilder();
        log.info("OPERATE-LOG Create JDBC connection for operation log...");
        return new JdbcTemplate(dataSourceBuilder.build());
    }

    public void initCheck(JdbcTemplate jdbcTemplate, String tableName) throws OperateLogException {
        log.info("OPERATE-LOG Initialization checks whether the database exists. Check whether the table exists.");
        try {
            Integer result = jdbcTemplate.queryForObject(checkDatabaseSql, Integer.class);
            if (1 != result) {
                throw new OperateLogException();
            }
        } catch (DataAccessException e) {
            throw new OperateLogException("Error database not found! Please configure the correct database connection!");
        }
        // 检查表存在
        String databaseName = JdbcUrlUtils.findDatabaseName(dataSourceProperties.getUrl());
        Long value = jdbcTemplate.queryForObject(checkTableSql, Long.class, tableName, databaseName);
        if (value != null && value > 0) {
            log.info("OPERATE-LOG There is already an operation log table. There is no need to create a new table! Table's name is {}.", tableName);
            return;
        }
        log.info("OPERATE-LOG The operation log table does not exist yet. We are about to create a new table! Table's name is {}.", tableName);
        jdbcTemplate.update(String.format(createTableSql, tableName));
    }
}
