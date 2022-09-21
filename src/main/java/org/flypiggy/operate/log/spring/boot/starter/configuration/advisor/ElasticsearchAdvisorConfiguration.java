package org.flypiggy.operate.log.spring.boot.starter.configuration.advisor;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.flypiggy.operate.log.spring.boot.starter.configuration.AdvisorBase;
import org.flypiggy.operate.log.spring.boot.starter.configuration.EnableAdvisorConfiguration;
import org.flypiggy.operate.log.spring.boot.starter.datasource.impl.ElasticsearchRepository;
import org.flypiggy.operate.log.spring.boot.starter.properties.Elasticsearch;
import org.flypiggy.operate.log.spring.boot.starter.properties.OperateLog;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Objects;

@Slf4j
@Configuration
@ConditionalOnBean(name = "enableEs")
@AutoConfigureAfter(EnableAdvisorConfiguration.class)
public class ElasticsearchAdvisorConfiguration extends AdvisorBase {

    public ElasticsearchAdvisorConfiguration(OperateLog operateLog) {
        super(operateLog);
    }

    @Bean
    public AspectJExpressionPointcutAdvisor esConfigurableAdvisor() {
        ElasticsearchClient client = this.getElasticsearchClient(operateLog.getElasticsearch());
        Elasticsearch.Index index = operateLog.getElasticsearch().getIndex();
        return getPointcutAdvisor(new ElasticsearchRepository(client, index));
    }

    private ElasticsearchClient getElasticsearchClient(Elasticsearch elasticsearch) {
        HttpHost[] httpHosts = Arrays.stream(elasticsearch.getNodes()).map(x -> {
            String[] hostInfo = x.split(":");
            return new HttpHost(hostInfo[0], Integer.parseInt(hostInfo[1]));
        }).toArray(HttpHost[]::new);
        RestClientBuilder builder;
        if (Objects.isNull(elasticsearch.getUsername()) || "".equals(elasticsearch.getUsername())) {
            builder = RestClient.builder(httpHosts);
        } else {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(elasticsearch.getUsername(), elasticsearch.getPassword()));
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
