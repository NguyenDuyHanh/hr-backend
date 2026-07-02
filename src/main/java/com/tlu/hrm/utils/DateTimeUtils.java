package com.tlu.hrm.utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * General date and time utility class for the HRM system.
 */
public class DateTimeUtils {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static String formatShiftTime(LocalTime time) {
        if (time == null) return "";
        return time.format(TIME_FORMATTER);
    }

    public static String getDayOfWeekVietnamese(LocalDate date) {
        if (date == null) return "";
        return switch (date.getDayOfWeek()) {
            case MONDAY -> "Thứ Hai";
            case TUESDAY -> "Thứ Ba";
            case WEDNESDAY -> "Thứ Tư";
            case THURSDAY -> "Thứ Năm";
            case FRIDAY -> "Thứ Sáu";
            case SATURDAY -> "Thứ Bảy";
            case SUNDAY -> "Chủ Nhật";
        };
    }
}
