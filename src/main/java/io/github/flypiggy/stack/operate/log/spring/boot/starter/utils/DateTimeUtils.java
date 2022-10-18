package io.github.flypiggy.stack.operate.log.spring.boot.starter.utils;

import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Date;

@NoArgsConstructor
public class DateTimeUtils {

    public static String format(Date date, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(date);
    }
}
