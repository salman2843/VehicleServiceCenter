package com.vsc.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Money helper. The whole project uses {@link BigDecimal} for currency so that
 * invoice totals never drift due to binary floating point. All amounts in the
 * model are stored as BigDecimal.
 */
public final class Money {

    public static final BigDecimal ZERO = BigDecimal.ZERO;

    private Money() {
        // utility class, no instances
    }

    /** Scale used for stored monetary values (2 decimal places, e.g. rupees/paise). */
    public static final int SCALE = 2;

    public static BigDecimal of(double value) {
        return BigDecimal.valueOf(value).setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        return a.add(b).setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
        return a.subtract(b).setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal multiply(BigDecimal a, BigDecimal b) {
        return a.multiply(b).setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isNegative(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) < 0;
    }
}