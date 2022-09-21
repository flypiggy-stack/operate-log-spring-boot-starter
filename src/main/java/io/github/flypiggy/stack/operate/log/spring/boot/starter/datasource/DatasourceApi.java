package io.github.flypiggy.stack.operate.log.spring.boot.starter.datasource;

import io.github.flypiggy.stack.operate.log.spring.boot.starter.model.Log;

public interface DatasourceApi {

    void save(Log log);
}
