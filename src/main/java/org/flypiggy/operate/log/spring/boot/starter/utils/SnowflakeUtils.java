package org.flypiggy.operate.log.spring.boot.starter.utils;

import cn.hutool.core.lang.Snowflake;

public class SnowflakeUtils {

    private static final Snowflake snowflake;

    static {
        snowflake = new Snowflake();
    }

    public static Long getId() {
        return snowflake.nextId();
    }
}
