package com.chargev.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateTimeFormatHelper {
    
    private static final DateTimeFormatter CUSTOM_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .withZone(ZoneId.of("UTC"));

    private static final DateTimeFormatter SIMPLE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .withZone(ZoneId.of("UTC"));

    public static String formatToCustomStyle(ZonedDateTime dateTime) {
        return dateTime.format(CUSTOM_FORMATTER);
    }

    public static String formatToSimpleStyle(ZonedDateTime dateTime) {
        return dateTime.format(SIMPLE_FORMATTER);
    }
    public static String formatToCustomStyle(Date dateTime) {
        if(dateTime == null){
            return null;
        }
          Instant instant = dateTime.toInstant();
        // Instant를 ZonedDateTime으로 변환 (기본 시간대를 사용)
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("UTC")); 
        return zonedDateTime.format(CUSTOM_FORMATTER);
    }

    public static String formatToSimpleStyle(Date dateTime) {
        if(dateTime == null){
            return null;
        }
        Instant instant = dateTime.toInstant();
      // Instant를 ZonedDateTime으로 변환 (기본 시간대를 사용)
      ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("UTC")); 
        return zonedDateTime.format(SIMPLE_FORMATTER);
    }
}
