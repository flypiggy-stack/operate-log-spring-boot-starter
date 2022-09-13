package org.flypiggy.operate.log.spring.boot.starter.utils;

import org.flypiggy.operate.log.spring.boot.starter.exception.OperateLogException;

public class JdbcUrlUtils {
    public static String findDatabaseName(String jdbcUrl) {
        String database = "";
        int pos, pos1;
        String connUri;
        if (!jdbcUrl.startsWith("jdbc:") || (pos1 = jdbcUrl.indexOf(':', 5)) == -1) {
            throw new OperateLogException("Invalid JDBC url.");
        }

        connUri = jdbcUrl.substring(pos1 + 1);

        if (connUri.startsWith("//")) {
            if ((pos = connUri.indexOf('/', 2)) != -1) {
                database = connUri.substring(pos + 1);
            }
        } else {
            database = connUri;
        }

        if (database.contains("?")) {
            database = database.substring(0, database.indexOf("?"));
        }

        if (database.contains(";")) {
            database = database.substring(0, database.indexOf(";"));
        }

        if (database.length() == 0) {
            throw new OperateLogException("Invalid JDBC url.");
        }
        return database;
    }
}
