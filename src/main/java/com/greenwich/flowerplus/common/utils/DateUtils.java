package com.greenwich.flowerplus.common.utils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateUtils {

    private DateUtils() {
    }

    public static String formatDateToString(Instant date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    /**
     * Formats an Instant to a human-readable format with timezone.
     * Example: "December 20, 2024 at 2:45 PM (GMT+7)"
     *
     * @param instant the instant to format
     * @param zoneId the timezone (e.g., ZoneId.of("Asia/Bangkok") for GMT+7)
     * @return formatted date string
     */
    public static String formatWithTimezone(Instant instant, ZoneId zoneId) {
        ZonedDateTime zonedDateTime = instant.atZone(zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                "MMMM d, yyyy 'at' h:mm a (O)",
                Locale.ENGLISH
        );
        return zonedDateTime.format(formatter);
    }

    /**
     * Formats an Instant using system default timezone.
     * Example: "December 20, 2024 at 2:45 PM (GMT+7)"
     *
     * @param instant the instant to format
     * @return formatted date string
     */
    public static String formatWithTimezone(Instant instant) {
        return formatWithTimezone(instant, ZoneId.systemDefault());
    }

    /**
     * Formats an Instant to GMT+7 timezone (Asia/Bangkok).
     * Example: "December 20, 2024 at 2:45 PM (GMT+7)"
     *
     * @param instant the instant to format
     * @return formatted date string
     */
    public static String formatToGMT7(Instant instant) {
        return formatWithTimezone(instant, ZoneId.of("Asia/Bangkok"));
    }
}
