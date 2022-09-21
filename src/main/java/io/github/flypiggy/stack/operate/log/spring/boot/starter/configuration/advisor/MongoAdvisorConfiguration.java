package io.github.flypiggy.stack.operate.log.spring.boot.starter.configuration.advisor;

import io.github.flypiggy.stack.operate.log.spring.boot.starter.configuration.AdvisorBase;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.configuration.EnableAdvisorConfiguration;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.datasource.impl.MongodbRepository;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.properties.OperateLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Slf4j
@Configuration
@ConditionalOnBean(name = "enableMongo")
@AutoConfigureAfter(EnableAdvisorConfiguration.class)
public class MongoAdvisorConfiguration extends AdvisorBase {

    public MongoAdvisorConfiguration(OperateLog operateLog) {
        super(operateLog);
    }

    @Bean
    public AspectJExpressionPointcutAdvisor mongodbConfigurableAdvisor(MongoTemplate mongoTemplate) {
        return getPointcutAdvisor(new MongodbRepository(mongoTemplate.getCollection(operateLog.getMongodb().getCollectionName())));
    }
}
