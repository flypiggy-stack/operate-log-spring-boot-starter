package org.flypiggy.operate.log.spring.boot.starter.model;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class Log {
    /**
     * primary key
     */
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
    private ZonedDateTime createTime = ZonedDateTime.now();
    /**
     * update time
     */
    private ZonedDateTime updateTime = ZonedDateTime.now();

}
