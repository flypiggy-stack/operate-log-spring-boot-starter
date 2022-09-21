package io.github.flypiggy.stack.operate.log.spring.boot.starter.datasource.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.datasource.DatasourceApi;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.model.Log;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.properties.DateSuffixEnum;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.properties.Elasticsearch;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.properties.EsIndexTypeEnum;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.UUID;

@Slf4j
public class ElasticsearchRepository implements DatasourceApi {

    private final ElasticsearchClient client;
    private final DateSuffixEnum dateSuffixEnum;
    private final Boolean haveSuffix;
    private final String baseIndex;

    public ElasticsearchRepository(ElasticsearchClient client, Elasticsearch.Index index) {
        this.client = client;
        this.dateSuffixEnum = index.getSuffix();
        this.baseIndex = index.getName();
        this.haveSuffix = EsIndexTypeEnum.DATE_SUFFIX.equals(index.getType());
    }

    @Override
    public void save(Log logVo) {
        String index = baseIndex;
        if (haveSuffix) {
            String suffix = DateSuffixEnum.getSuffix(dateSuffixEnum);
            index = baseIndex + "_" + suffix;
        }
        try {
            String finalIndex = index;
            client.create(e -> e.index(finalIndex).id(UUID.randomUUID().toString()).document(logVo));
        } catch (IOException e) {
            log.warn("OPERATE-LOG Store elasticsearch document exception.");
        }
    }
}
