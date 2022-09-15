package org.flypiggy.operate.log.spring.boot.starter.repository;

import org.flypiggy.operate.log.spring.boot.starter.model.Log;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "spring.operate-log", name = "datasource-type", havingValue = "elasticsearch")
public interface LogRepository extends ElasticsearchRepository<Log, String> {
}
