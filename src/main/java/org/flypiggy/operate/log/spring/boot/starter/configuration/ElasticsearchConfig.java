package org.flypiggy.operate.log.spring.boot.starter.configuration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.flypiggy.operate.log.spring.boot.starter.exception.OperateLogException;

import java.io.IOException;

@Slf4j
@AllArgsConstructor
public class ElasticsearchConfig {

    public ElasticsearchClient getElasticsearchClient() {
        RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200)).build();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModules(new ParameterNamesModule(), new Jdk8Module(), new JavaTimeModule());
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper(objectMapper));
        log.info("OPERATE LOG Initialize elasticsearch data source connection.");
        return new ElasticsearchClient(transport);
    }

    /**
     * Initialization checks whether the index exists. If it does not exist, it is created.
     */
    public void initCheck(ElasticsearchClient client, String indexName) {
        log.info("OPERATE LOG Check whether the index[{}] exists.", indexName);
        try {
            ExistsRequest request = ExistsRequest.of(e -> e.index(indexName));
            BooleanResponse booleanResponse = client.indices().exists(request);
            if (booleanResponse.value()) {
                log.info("OPERATE-LOG There is already an operation log index. There is no need to create a new index! Index's name is {}.", indexName);
                return;
            }
            log.info("OPERATE-LOG The operation log index does not exist yet. We are about to create a new index! Index's name is {}.", indexName);
            CreateIndexResponse createIndexResponse = client.indices().create(c -> c.index(indexName));
            System.out.println(createIndexResponse.acknowledged());
        } catch (IOException e) {
            throw new OperateLogException("The connection Elasticsearch the data source error, please check whether the connection can be normal.");
        }

    }
}
