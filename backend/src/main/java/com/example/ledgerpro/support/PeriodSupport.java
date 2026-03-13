package com.example.ledgerpro.support;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public final class PeriodSupport {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private PeriodSupport() {
    }

    public static String normalize(String period) {
        if (period == null || period.trim().isEmpty()) {
            return YearMonth.now().format(FORMATTER);
        }
        return YearMonth.parse(period.trim(), FORMATTER).format(FORMATTER);
    }

    public static YearMonth parse(String period) {
        return YearMonth.parse(normalize(period), FORMATTER);
    }
}
