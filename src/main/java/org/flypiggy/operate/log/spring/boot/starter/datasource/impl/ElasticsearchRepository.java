package org.flypiggy.operate.log.spring.boot.starter.datasource.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flypiggy.operate.log.spring.boot.starter.datasource.DatasourceApi;
import org.flypiggy.operate.log.spring.boot.starter.model.Log;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.io.IOException;

@Slf4j
@AllArgsConstructor
@ConditionalOnProperty(prefix = "spring.operate-log", name = "store-type", havingValue = "elasticsearch")
public class ElasticsearchRepository implements DatasourceApi {

    private final ElasticsearchClient client;
    private final String index;

    @Override
    public void save(Log logVo) {
        try {
            client.create(e -> e.index(index).document(logVo));
        } catch (IOException e) {
            log.warn("OPERATE-LOG Store elasticsearch document exception.");
        }
    }
}
