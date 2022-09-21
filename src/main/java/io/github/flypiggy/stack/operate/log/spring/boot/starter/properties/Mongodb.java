package io.github.flypiggy.stack.operate.log.spring.boot.starter.properties;

import lombok.Data;

@Data
public class Mongodb {
    /**
     * Mongodb collection name.
     */
    private String collectionName = "web_log";
}
