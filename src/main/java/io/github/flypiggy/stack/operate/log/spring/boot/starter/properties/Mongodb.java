package io.github.flypiggy.stack.operate.log.spring.boot.starter.properties;

public class Mongodb {
    /**
     * Mongodb collection name.
     */
    private String collectionName = "web_log";

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }
}
