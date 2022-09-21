package org.flypiggy.operate.log.spring.boot.starter.datasource.impl;

import com.mongodb.client.MongoCollection;
import lombok.AllArgsConstructor;
import org.bson.Document;
import org.flypiggy.operate.log.spring.boot.starter.datasource.DatasourceApi;
import org.flypiggy.operate.log.spring.boot.starter.model.Log;

@AllArgsConstructor
public class MongodbRepository implements DatasourceApi {

    private final MongoCollection<Document> mongoCollection;

    @Override
    public void save(Log log) {
        mongoCollection.insertOne(log2Document(log));
    }

    private Document log2Document(Log log) {
        return new Document()
                .append("id", log.getId())
                .append("ip", log.getIp())
                .append("operator", log.getOperator())
                .append("method", log.getMethod())
                .append("uri", log.getUri())
                .append("classInfo", log.getClassInfo())
                .append("methodInfo", log.getMethodInfo())
                .append("success", log.getSuccess())
                .append("requestBody", log.getRequestBody())
                .append("responseBody", log.getResponseBody())
                .append("errorMessage", log.getErrorMessage());
    }
}
