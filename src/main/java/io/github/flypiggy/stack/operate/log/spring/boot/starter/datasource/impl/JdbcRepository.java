package io.github.flypiggy.stack.operate.log.spring.boot.starter.datasource.impl;

import io.github.flypiggy.stack.operate.log.spring.boot.starter.datasource.DatasourceApi;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.model.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcRepository implements DatasourceApi {

    private final Logger log = LoggerFactory.getLogger(ElasticsearchRepository.class);

    private final JdbcTemplate operateLogJdbcTemplate;
    private final String insertSql;

    public JdbcRepository(JdbcTemplate operateLogJdbcTemplate, String insertSql) {
        this.operateLogJdbcTemplate = operateLogJdbcTemplate;
        this.insertSql = insertSql;
    }

    @Override
    public void save(Log logVo) {
        try {
            operateLogJdbcTemplate.update(insertSql, logVo.getId(), logVo.getIp(), logVo.getOperator(), logVo.getMethod(), logVo.getUri(), logVo.getClassInfo(),
                    logVo.getMethodInfo(), logVo.getSuccess(), logVo.getRequestBody(), logVo.getResponseBody(), logVo.getErrorMessage(), logVo.getTimeTaken());
        } catch (DataAccessException e) {
            log.warn("OPERATE-LOG JDBC insert log record error.This exception will not affect your main program flow, but operation logging cannot be saved.", e);
        }
    }
}
