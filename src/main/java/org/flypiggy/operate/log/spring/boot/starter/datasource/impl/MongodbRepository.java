package org.flypiggy.operate.log.spring.boot.starter.datasource.impl;

import lombok.AllArgsConstructor;
import org.flypiggy.operate.log.spring.boot.starter.datasource.DatasourceApi;
import org.flypiggy.operate.log.spring.boot.starter.model.Log;
import org.flypiggy.operate.log.spring.boot.starter.model.mongoDB.MongoLog;
import org.springframework.data.mongodb.core.MongoTemplate;

@AllArgsConstructor
public class MongodbRepository implements DatasourceApi {

    private final MongoTemplate mongoTemplate;

    @Override
    public void save(Log log) {
        mongoTemplate.save(log2MongLog(log));
    }

    private MongoLog log2MongLog(Log log) {
        MongoLog mongoLog = new MongoLog();
        mongoLog.setId(log.getId());
        mongoLog.setIp(log.getIp());
        mongoLog.setOperator(log.getOperator());
        mongoLog.setMethod(log.getMethod());
        mongoLog.setUri(log.getUri());
        mongoLog.setClassInfo(log.getClassInfo());
        mongoLog.setMethodInfo(log.getMethodInfo());
        mongoLog.setSuccess(log.getSuccess());
        mongoLog.setRequestBody(log.getRequestBody());
        mongoLog.setResponseBody(log.getResponseBody());
        mongoLog.setErrorMessage(log.getErrorMessage());
        return mongoLog;
    }
}
