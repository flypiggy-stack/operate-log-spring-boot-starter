package io.github.flypiggy.stack.operate.log.spring.boot.starter.properties;

import org.springframework.boot.context.properties.NestedConfigurationProperty;

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

    public TransportProtocolEnum getSchema() {
        return schema;
    }

    public void setSchema(TransportProtocolEnum schema) {
        this.schema = schema;
    }

    public String[] getNodes() {
        return nodes;
    }

    public void setNodes(String[] nodes) {
        this.nodes = nodes;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public void setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    public int getMaxConnectNum() {
        return maxConnectNum;
    }

    public void setMaxConnectNum(int maxConnectNum) {
        this.maxConnectNum = maxConnectNum;
    }

    public int getMaxConnectPerRoute() {
        return maxConnectPerRoute;
    }

    public void setMaxConnectPerRoute(int maxConnectPerRoute) {
        this.maxConnectPerRoute = maxConnectPerRoute;
    }

    public Index getIndex() {
        return index;
    }

    public void setIndex(Index index) {
        this.index = index;
    }

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

        public EsIndexTypeEnum getType() {
            return type;
        }

        public void setType(EsIndexTypeEnum type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public DateSuffixEnum getSuffix() {
            return suffix;
        }

        public void setSuffix(DateSuffixEnum suffix) {
            this.suffix = suffix;
        }
    }
}
