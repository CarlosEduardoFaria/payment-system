package com.eliasnogueira.paymentservice.validator;

import java.math.BigDecimal;

public final class PaymentLimitValidator {

    private PaymentLimitValidator() {}

    private static final BigDecimal MAX_LIMIT = new BigDecimal("2000.00");

    public static boolean isWithinLimit(BigDecimal amount) {
        if (amount == null) return false;

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        return amount.compareTo(MAX_LIMIT) <= 0;
    }
}
