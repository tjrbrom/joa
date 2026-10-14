package com.x.hhqs.joa.alarm.utils;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Date;

public class DateTimeUtils {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
            .withResolverStyle(ResolverStyle.STRICT);

    public static LocalDateTime parseDateTimeStrGet(String dateTimeStr) {
        try {
            return LocalDateTime.parse(dateTimeStr, FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    public static Date parseDate(String s) {
        if (s == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        sdf.setLenient(false);
        return sdf.parse(s, new ParsePosition(0));
    }

    public static String dateTimeConverter (String input){
        OffsetDateTime odt = OffsetDateTime.parse(input);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        return odt.format(formatter);
    }

    /**
     *
     * @param localDateTime 2025-06-15 10:00:00
     *
     * @return 2025-06-15T10:00:00.000+08:00
     */
    public static String formatterUTCDate(LocalDateTime localDateTime) {
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        return zonedDateTime.format(outputFormatter);
    }
}
