package org.flypiggy.operate.log.spring.boot.starter.properties;

import lombok.Data;

@Data
public class Elasticsearch {

    /**
     * elasticsearch index name
     */
    private String indexName = "operate_log";
}
