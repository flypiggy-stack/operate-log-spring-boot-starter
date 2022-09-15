package org.flypiggy.operate.log.spring.boot.starter.datasource.impl;

import lombok.AllArgsConstructor;
import org.flypiggy.operate.log.spring.boot.starter.datasource.DatasourceApi;
import org.flypiggy.operate.log.spring.boot.starter.model.Log;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@ConditionalOnProperty(prefix = "spring.operate-log", name = "datasource-type", havingValue = "jdbc")
public class JdbcRepository implements DatasourceApi {

    private JdbcTemplate operateLogJdbcTemplate;

    @Override
    public void save(Log logVo, String sql) {
        operateLogJdbcTemplate.update(sql, logVo.getIp(), logVo.getOperator(), logVo.getMethod(), logVo.getUri(), logVo.getClassInfo(),
                logVo.getMethodInfo(), logVo.getSuccess(), logVo.getRequestBody(), logVo.getResponseBody(), logVo.getErrorMessage());
    }
}
