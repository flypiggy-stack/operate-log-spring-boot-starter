package io.github.flypiggy.stack.operate.log.spring.boot.starter.datasource.impl;

import com.mongodb.client.MongoCollection;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.advice.WebLogAdvice;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.datasource.DatasourceApi;
import io.github.flypiggy.stack.operate.log.spring.boot.starter.model.Log;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;

public class MongodbRepository implements DatasourceApi {

    private final Logger logger = LoggerFactory.getLogger(WebLogAdvice.class);

    private final MongoCollection<Document> mongoCollection;

    public MongodbRepository(MongoCollection<Document> mongoCollection) {
        this.mongoCollection = mongoCollection;
    }

    @Override
    public void save(Log log) {
        try {
            mongoCollection.insertOne(log2Document(log));
        } catch (Exception e) {
            logger.warn("OPERATE-LOG Mongo insert log record error.This exception will not affect your main program flow, but operation logging cannot be saved.", e);
        }
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
                .append("errorMessage", log.getErrorMessage())
                .append("timeTaken", log.getTimeTaken())
                .append("createTime", ZonedDateTime.now().toString())
                .append("updateTime", ZonedDateTime.now().toString());
    }
}
