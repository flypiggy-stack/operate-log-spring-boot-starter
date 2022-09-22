package io.github.flypiggy.stack.operate.log.spring.boot.starter.datasource.impl;

import io.github.flypiggy.stack.operate.log.spring.boot.starter.datasource.DatasourceApi;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.model.Log;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

@AllArgsConstructor
public class JdbcRepository implements DatasourceApi {

    private final JdbcTemplate operateLogJdbcTemplate;
    private final String insertSql;

    @Override
    public void save(Log logVo) {
        operateLogJdbcTemplate.update(insertSql, logVo.getId(), logVo.getIp(), logVo.getOperator(), logVo.getMethod(), logVo.getUri(), logVo.getClassInfo(),
                logVo.getMethodInfo(), logVo.getSuccess(), logVo.getRequestBody(), logVo.getResponseBody(), logVo.getErrorMessage());
    }
}