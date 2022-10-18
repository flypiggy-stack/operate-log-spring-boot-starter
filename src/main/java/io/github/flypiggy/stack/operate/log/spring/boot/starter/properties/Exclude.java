package io.github.flypiggy.stack.operate.log.spring.boot.starter.properties;

import org.springframework.http.HttpMethod;

import java.util.Map;


public class Exclude {

    /**
     * Apis to be excluded.
     * format {k: get v: /xxx/xxx/xx}
     */
    private Map<HttpMethod, String[]> api;

    /**
     * Request method.
     */
    private HttpMethod[] httpMethod;

    public Map<HttpMethod, String[]> getApi() {
        return api;
    }

    public void setApi(Map<HttpMethod, String[]> api) {
        this.api = api;
    }

    public HttpMethod[] getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod[] httpMethod) {
        this.httpMethod = httpMethod;
    }
}
