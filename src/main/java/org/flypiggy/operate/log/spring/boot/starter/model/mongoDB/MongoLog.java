package org.flypiggy.operate.log.spring.boot.starter.model.mongoDB;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "#{@collectionName}")
public class MongoLog {
    /**
     * primary key
     */
    @Id
    private Long id;
    /**
     * request source IP address
     */
    private String ip;
    /**
     * operator
     */
    private String operator;
    /**
     * request method
     */
    private String method;
    /**
     * request URI
     */
    private String uri;
    /**
     * class info
     */
    private String classInfo;
    /**
     * method info
     */
    private String methodInfo;
    /**
     * success for true or false
     */
    private Boolean success;
    /**
     * requestor
     */
    private String requestBody;
    /**
     * responder
     */
    private String responseBody;
    /**
     * error message
     */
    private String errorMessage;
    /**
     * create time
     */
    private Date createTime = new Date();
    /**
     * update time
     */
    private Date updateTime = new Date();

}