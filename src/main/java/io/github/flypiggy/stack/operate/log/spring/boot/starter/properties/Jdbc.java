package io.github.flypiggy.stack.operate.log.spring.boot.starter.properties;

public class Jdbc {

    /**
     * Table name of the storage operation record.
     */
    private String tableName = "web_log";

    /**
     * create table SQL, fill in this configuration according to the specific database of the connection.
     */
    private String createTableSql = "create table %s\n" +
            "(\n" +
            "    id                bigint unsigned primary key                not null comment 'primary key',\n" +
            "    ip                varchar(128)                               not null comment 'ip address',\n" +
            "    operator          varchar(256)                               null comment 'operator',\n" +
            "    method            varchar(20)                                not null comment 'request method',\n" +
            "    uri               varchar(256)                               not null comment 'request URI',\n" +
            "    class_info        varchar(256)                               not null comment 'class info',\n" +
            "    method_info       varchar(256)                               not null comment 'method info',\n" +
            "    success           tinyint(1)                                 not null comment '1:success 0:failure',\n" +
            "    request_body      text                                       null comment 'requestor',\n" +
            "    response_body     text                                       null comment 'responder',\n" +
            "    error_message     text                                       null comment 'error message',\n" +
            "    time_taken        bigint                                     null comment 'time taken,unit ms',\n" +
            "    create_time       datetime  default CURRENT_TIMESTAMP        not null comment 'create time',\n" +
            "    update_time       datetime  default CURRENT_TIMESTAMP        not null on update CURRENT_TIMESTAMP comment 'update time'\n" +
            ")";

    /**
     * Check SQL for whether exists in the table.
     */
    private String checkTableSql = "select count(1) from information_schema.tables where table_name = '%s' and table_schema = '%s' limit 1";

    /**
     * Check SQL for is there a database.
     */
    private String checkDatabaseSql = "select 1";

    /**
     * Add the SQL statement of the operation log.
     */
    private String insertSql = "insert into %s (id, ip, operator, method, uri, class_info, method_info, success, request_body, response_body, error_message, time_taken) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public String getCreateTableSql() {
        return createTableSql.contains("%s") ? String.format(createTableSql, tableName) : createTableSql;
    }

    public String getInsertSql() {
        return insertSql.contains("%s") ? String.format(insertSql, tableName) : insertSql;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setCreateTableSql(String createTableSql) {
        this.createTableSql = createTableSql;
    }

    public String getCheckTableSql() {
        return checkTableSql;
    }

    public void setCheckTableSql(String checkTableSql) {
        this.checkTableSql = checkTableSql;
    }

    public String getCheckDatabaseSql() {
        return checkDatabaseSql;
    }

    public void setCheckDatabaseSql(String checkDatabaseSql) {
        this.checkDatabaseSql = checkDatabaseSql;
    }

    public void setInsertSql(String insertSql) {
        this.insertSql = insertSql;
    }
}
