package io.github.flypiggy.stack.operate.log.spring.boot.starter.model;

import io.github.flypiggy.stack.operate.log.spring.boot.starter.utils.SnowflakeUtils;

import java.time.ZonedDateTime;

public class Log {
    /**
     * primary key
     */
    private Long id = SnowflakeUtils.getId();
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
     * time taken, unit ms
     */
    private Long timeTaken;
    /**
     * create time
     */
    private ZonedDateTime createTime = ZonedDateTime.now();
    /**
     * update time
     */
    private ZonedDateTime updateTime = ZonedDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getClassInfo() {
        return classInfo;
    }

    public void setClassInfo(String classInfo) {
        this.classInfo = classInfo;
    }

    public String getMethodInfo() {
        return methodInfo;
    }

    public void setMethodInfo(String methodInfo) {
        this.methodInfo = methodInfo;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(Long timeTaken) {
        this.timeTaken = timeTaken;
    }

    public ZonedDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(ZonedDateTime createTime) {
        this.createTime = createTime;
    }

    public ZonedDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(ZonedDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "Log{" +
                "id=" + id +
                ", ip='" + ip + '\'' +
                ", operator='" + operator + '\'' +
                ", method='" + method + '\'' +
                ", uri='" + uri + '\'' +
                ", classInfo='" + classInfo + '\'' +
                ", methodInfo='" + methodInfo + '\'' +
                ", success=" + success +
                ", requestBody='" + requestBody + '\'' +
                ", responseBody='" + responseBody + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", timeTaken=" + timeTaken +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
