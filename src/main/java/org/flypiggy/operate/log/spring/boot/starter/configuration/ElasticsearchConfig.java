package org.flypiggy.operate.log.spring.boot.starter.configuration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.flypiggy.operate.log.spring.boot.starter.properties.Elasticsearch;

import java.util.Arrays;
import java.util.Objects;

@Slf4j
@AllArgsConstructor
public class ElasticsearchConfig {

    public ElasticsearchClient getElasticsearchClient(Elasticsearch elasticsearch) {
        HttpHost[] httpHosts = Arrays.stream(elasticsearch.getNodes()).map(x -> {
            String[] hostInfo = x.split(":");
            return new HttpHost(hostInfo[0], Integer.parseInt(hostInfo[1]));
        }).toArray(HttpHost[]::new);
        RestClientBuilder builder;
        if (Objects.isNull(elasticsearch.getAccount()) || "".equals(elasticsearch.getAccount())) {
            builder = RestClient.builder(httpHosts);
        } else {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(elasticsearch.getPassword(), elasticsearch.getPassword()));
            builder = RestClient.builder(httpHosts)
                    .setHttpClientConfigCallback(httpClientBuilder ->
                            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }
        RestClient restClient = builder.build();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModules(new ParameterNamesModule(), new Jdk8Module(), new JavaTimeModule());
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper(objectMapper));
        log.info("OPERATE-LOG Initialize elasticsearch data source connection.");
        return new ElasticsearchClient(transport);
    }
}
