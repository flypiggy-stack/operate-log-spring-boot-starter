package org.flypiggy.operate.log.spring.boot.starter.properties;


import org.apache.commons.lang3.time.DateFormatUtils;

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
                suffix = DateFormatUtils.format(new Date(), "yyyy.MM.dd");
                break;
            case MONTH:
                suffix = DateFormatUtils.format(new Date(), "yyyy.MM");
                break;
            case YEAR:
                suffix = DateFormatUtils.format(new Date(), "yyyy");
        }
        return suffix;
    }
}
