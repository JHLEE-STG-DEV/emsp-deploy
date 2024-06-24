package com.chargev.emsp.service.formatter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

@Service
public class DateTimeFormatterService {

    private static final DateTimeFormatter CUSTOM_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .withZone(ZoneId.of("UTC"));

    private static final DateTimeFormatter SIMPLE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .withZone(ZoneId.of("UTC"));

    public String formatToCustomStyle(ZonedDateTime dateTime) {
        return dateTime.format(CUSTOM_FORMATTER);
    }

    public String formatToSimpleStyle(ZonedDateTime dateTime) {
        return dateTime.format(SIMPLE_FORMATTER);
    }
}
