package io.github.flypiggy.stack.operate.log.spring.boot.starter.properties;


import io.github.flypiggy.stack.operate.log.spring.boot.starter.utils.DateTimeUtils;

import java.util.Date;

public enum DateSuffixEnum {
    /**
     * Year as index suffix.
     */
    YEAR,
    /**
     * Month as index suffix.
     */
    MONTH,
    /**
     * Day as index suffix.
     */
    DAY;

    public static String getSuffix(DateSuffixEnum suffixEnum) {
        String suffix = "";
        switch (suffixEnum) {
            case DAY:
                suffix = DateTimeUtils.format(new Date(), "yyyy.MM.dd");
                break;
            case MONTH:
                suffix = DateTimeUtils.format(new Date(), "yyyy.MM");
                break;
            case YEAR:
                suffix = DateTimeUtils.format(new Date(), "yyyy");
        }
        return suffix;
    }
}
