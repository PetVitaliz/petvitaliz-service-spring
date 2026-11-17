package com.petvitaliz.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TimeParser {

    private static final Pattern TIME = Pattern.compile("^([01]?\\d|2[0-3]):([0-5]\\d)$");
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private TimeParser() {
    }

    public static Optional<LocalTime> parse(String text) {
        if (text == null) {
            return Optional.empty();
        }
        Matcher matcher = TIME.matcher(text.trim());
        if (!matcher.matches()) {
            return Optional.empty();
        }
        return Optional.of(LocalTime.of(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))));
    }

    public static String format(LocalTime time) {
        return time.format(FORMAT);
    }
}
