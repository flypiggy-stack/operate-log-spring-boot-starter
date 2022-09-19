package org.flypiggy.operate.log.spring.boot.starter.datasource.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flypiggy.operate.log.spring.boot.starter.datasource.DatasourceApi;
import org.flypiggy.operate.log.spring.boot.starter.model.Log;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@AllArgsConstructor
@ConditionalOnProperty(prefix = "spring.operate-log", name = "datasource-type", havingValue = "elasticsearch")
public class ElasticsearchRepository implements DatasourceApi {

    private final String index;
    private final ElasticsearchClient client;

    @Override
    public void save(Log logVo) {
        try {
            client.create(e -> e.index(index).document(logVo));
        } catch (IOException e) {
            log.warn("OPERATE-LOG Store elasticsearch document exception.");
        }
    }
}
