package mz.co.amaterasu.gestao.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {

    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private Money() {
    }

    public static BigDecimal of(BigDecimal value) {
        if (value == null) {
            return ZERO;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal qty(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ONE.setScale(3, RoundingMode.HALF_UP);
        }
        return value.setScale(3, RoundingMode.HALF_UP);
    }

    public static BigDecimal rate(BigDecimal value) {
        if (value == null) {
            return new BigDecimal("16.00");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
