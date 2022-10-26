package io.github.flypiggy.stack.operate.log.spring.boot.starter.configuration;

import io.github.flypiggy.stack.operate.log.spring.boot.starter.properties.OperateLog;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "spring.operate-log", name = "enable", havingValue = "true")
@AutoConfigureAfter(OperateLog.class)
public class EnableAdvisorConfiguration {

    public EnableAdvisorConfiguration() {
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.operate-log", name = "store-type", havingValue = "mongodb")
    public void enableMongo() {
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.operate-log", name = "store-type", havingValue = "elasticsearch")
    public void enableEs() {
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.operate-log", name = "store-type", havingValue = "jdbc")
    public void enableJdbc() {
    }
}
