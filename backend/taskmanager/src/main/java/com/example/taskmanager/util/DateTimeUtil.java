package com.example.taskmanager.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class DateTimeUtil {

    private static final ZoneId GREECE_ZONE = ZoneId.of("Europe/Athens");

    // Convert UTC Instant to LocalDateTime in Greek timezone
    public static LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) return null;
        return LocalDateTime.ofInstant(instant, GREECE_ZONE);
    }

    // Convert LocalDateTime in Greece to UTC Instant (for DB storage)
    public static Instant toInstant(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return localDateTime.atZone(GREECE_ZONE).toInstant();
    }

    // Current timestamp (UTC)
    public static Instant nowUtc() {
        return Instant.now();
    }

    // Current local datetime (Greece)
    public static LocalDateTime nowLocal() {
        return LocalDateTime.now(GREECE_ZONE);
    }

    // Optional: formatted local string
    public static String formatLocal(Instant instant) {
        if (instant == null) return null;
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(GREECE_ZONE)
                .format(instant);
    }

    // Optional global default timezone
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
}
