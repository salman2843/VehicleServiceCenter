package com.vsc.util;

/**
 * Small, dependency-free input validation helper. Service and UI layers use
 * this so validation rules live in one place and can be unit tested.
 */
public final class InputValidator {

    private InputValidator() {
        // utility class, no instances
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isValidName(String name) {
        if (isBlank(name)) {
            return false;
        }
        return name.trim().length() >= 2 && name.trim().length() <= 80;
    }

    public static boolean isValidPhone(String phone) {
        if (isBlank(phone)) {
            return false;
        }
        String digits = phone.replaceAll("[^0-9]", "");
        return digits.length() >= 7 && digits.length() <= 15;
    }

    public static boolean isValidRegistrationNo(String regNo) {
        if (isBlank(regNo)) {
            return false;
        }
        String normalized = regNo.trim().toUpperCase().replaceAll("\\s+", "");
        return normalized.length() >= 4 && normalized.length() <= 16;
    }

    public static boolean isPositive(int value) {
        return value > 0;
    }

    public static boolean isNonNegative(int value) {
        return value >= 0;
    }

    public static String trim(String value) {
        return value == null ? null : value.trim();
    }
}