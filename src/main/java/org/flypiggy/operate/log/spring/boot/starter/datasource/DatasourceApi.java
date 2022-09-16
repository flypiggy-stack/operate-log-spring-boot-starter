package org.flypiggy.operate.log.spring.boot.starter.datasource;

import org.flypiggy.operate.log.spring.boot.starter.model.Log;

public interface DatasourceApi {

    void save(Log log);
}
