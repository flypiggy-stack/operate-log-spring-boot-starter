package io.github.flypiggy.stack.operate.log.spring.boot.starter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Data
public class Elasticsearch {
    /**
     * protocol
     */
    private TransportProtocolEnum schema = TransportProtocolEnum.HTTP;
    /**
     * elasticsearch host array.
     */
    private String[] nodes = new String[]{"localhost:9200"};
    /**
     * elasticsearch username.
     */
    private String username;
    /**
     * elasticsearch password.
     */
    private String password;
    /**
     * Connection timeout.
     */
    private int connectTimeout = 10000;
    /**
     * Socket connection timeout.
     */
    private int socketTimeout = 15000;
    /**
     * Gets the timeout of the connection
     */
    private int connectionRequestTimeout = 20000;
    /**
     * Maximum number of connections.
     */
    private int maxConnectNum = 100;
    /**
     * Maximum number of routing connections.
     */
    private int maxConnectPerRoute = 100;
    /**
     * elasticsearch index.
     */
    @NestedConfigurationProperty
    private Index index = new Index();

    @Data
    public static class Index {
        /**
         * elasticsearch index type.
         */
        private EsIndexTypeEnum type = EsIndexTypeEnum.FINAL_UNCHANGED;
        /**
         * elasticsearch index name.
         * This configuration takes effect only when an index of type 'FINAL_UNCHANGED' is selected.
         */
        private String name = "web_log";
        /**
         * This configuration takes effect only when an index of type 'DATE_SUFFIX' is selected.
         * The specific index name will be prefixed with the 'name' attribute and the date as the suffix.
         */
        private DateSuffixEnum suffix = DateSuffixEnum.YEAR;
    }

}
