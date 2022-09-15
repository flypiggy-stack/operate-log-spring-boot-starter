package org.flypiggy.operate.log.spring.boot.starter.properties;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class Elasticsearch {
    /**
     * elasticsearch index name
     */
    // @Value("${spring.operate-log.elasticsearch.index-name:operate_log}")
    private String indexName = "operate_log";
}
