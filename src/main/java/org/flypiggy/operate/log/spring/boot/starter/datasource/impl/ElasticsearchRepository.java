package org.flypiggy.operate.log.spring.boot.starter.datasource.impl;

import lombok.AllArgsConstructor;
import org.flypiggy.operate.log.spring.boot.starter.datasource.DatasourceApi;
import org.flypiggy.operate.log.spring.boot.starter.model.Log;
import org.flypiggy.operate.log.spring.boot.starter.repository.LogRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@ConditionalOnProperty(prefix = "spring.operate-log", name = "datasource-type", havingValue = "elasticsearch")
public class ElasticsearchRepository implements DatasourceApi {

    private final LogRepository logRepository;

    @Override
    public void save(Log logVo, String sql) {
        logRepository.save(logVo);
    }
}
