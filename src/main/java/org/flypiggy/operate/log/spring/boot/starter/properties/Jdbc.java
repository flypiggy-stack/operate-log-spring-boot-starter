package org.flypiggy.operate.log.spring.boot.starter.properties;

import lombok.Data;

@Data
public class Jdbc {

    /**
     * Table name of the storage operation record.
     */
    private String tableName = "web_log";
}
