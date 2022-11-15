package io.github.flypiggy.stack.operate.log.spring.boot.starter.configuration.advisor;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.configuration.AdvisorBase;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.configuration.EnableAdvisorConfiguration;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.datasource.impl.ElasticsearchRepository;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.properties.Elasticsearch;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.properties.OperateLog;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Objects;

@Configuration
@ConditionalOnBean(name = "enableEs")
@AutoConfigureAfter(EnableAdvisorConfiguration.class)
public class ElasticsearchAdvisorConfiguration extends AdvisorBase {

    private final Logger log = LoggerFactory.getLogger(ElasticsearchAdvisorConfiguration.class);

    public ElasticsearchAdvisorConfiguration(OperateLog operateLog) {
        super(operateLog);
    }

    @Bean
    public AspectJExpressionPointcutAdvisor esConfigurableAdvisor() {
        ElasticsearchClient client = this.getElasticsearchClient(operateLog.getElasticsearch());
        Elasticsearch.Index index = operateLog.getElasticsearch().getIndex();
        return super.getPointcutAdvisor(new ElasticsearchRepository(client, index));
    }

    private ElasticsearchClient getElasticsearchClient(Elasticsearch elasticsearch) {
        HttpHost[] httpHosts = Arrays.stream(elasticsearch.getNodes()).map(x -> {
            String[] hostInfo = x.split(":");
            return new HttpHost(hostInfo[0], Integer.parseInt(hostInfo[1]), elasticsearch.getSchema().toString().toLowerCase());
        }).toArray(HttpHost[]::new);
        RestClientBuilder builder = RestClient.builder(httpHosts).setRequestConfigCallback(requestConfigBuilder -> {
            requestConfigBuilder.setConnectTimeout(elasticsearch.getConnectTimeout());
            requestConfigBuilder.setSocketTimeout(elasticsearch.getSocketTimeout());
            requestConfigBuilder.setConnectionRequestTimeout(elasticsearch.getConnectionRequestTimeout());
            return requestConfigBuilder;
        });
        if (Objects.isNull(elasticsearch.getUsername()) || "".equals(elasticsearch.getUsername())) {
            builder.setHttpClientConfigCallback(httpClientBuilder -> {
                httpClientBuilder.setMaxConnTotal(elasticsearch.getMaxConnectNum());
                httpClientBuilder.setMaxConnPerRoute(elasticsearch.getMaxConnectPerRoute());
                return httpClientBuilder;
            });
        } else {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(elasticsearch.getUsername(), elasticsearch.getPassword()));
            builder.setHttpClientConfigCallback(httpClientBuilder -> {
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                httpClientBuilder.setMaxConnTotal(elasticsearch.getMaxConnectNum());
                httpClientBuilder.setMaxConnPerRoute(elasticsearch.getMaxConnectPerRoute());
                return httpClientBuilder;
            });
        }
        RestClient restClient = builder.build();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModules(new ParameterNamesModule(), new Jdk8Module(), new JavaTimeModule());
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper(objectMapper));
        log.info("OPERATE-LOG Initialize elasticsearch data source connection.");
        return new ElasticsearchClient(transport);
    }
}
