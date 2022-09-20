package org.flypiggy.operate.log.spring.boot.starter.properties;

import lombok.Data;

@Data
public class Mongodb {
    /**
     * Mongodb collection name.
     */
    private String collectionName = "web_log";
}
