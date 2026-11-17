package com.petvitaliz.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public final class DateParser {

    private static final int ISO_DATE_LENGTH = 10;

    private DateParser() {
    }

    public static Optional<LocalDate> parse(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        String value = text.trim();
        try {
            return Optional.of(LocalDate.parse(value.substring(0, Math.min(ISO_DATE_LENGTH, value.length()))));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    public static String toIsoMidnight(LocalDate date) {
        return date + "T00:00:00.000Z";
    }
}
