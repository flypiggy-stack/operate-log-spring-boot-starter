package io.github.flypiggy.stack.operate.log.spring.boot.starter.properties;

import lombok.Data;
import org.springframework.http.HttpMethod;

import java.util.Map;


@Data
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
}
