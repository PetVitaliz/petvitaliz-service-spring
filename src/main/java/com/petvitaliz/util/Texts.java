package com.petvitaliz.util;

import java.math.BigDecimal;

public final class Texts {

    private Texts() {
    }

    public static String trimToEmpty(String text) {
        return text == null ? "" : text.trim();
    }

    public static String normalizeEmail(String email) {
        return trimToEmpty(email).toLowerCase();
    }

    public static boolean isTrue(String text) {
        return "true".equalsIgnoreCase(trimToEmpty(text));
    }

    public static String fullName(String firstName, String lastName) {
        return (trimToEmpty(firstName) + " " + trimToEmpty(lastName)).trim();
    }

    public static String plainNumber(Number number) {
        return new BigDecimal(number.toString()).stripTrailingZeros().toPlainString();
    }
}
