package com.eliasnogueira.paymentservice.data;

import com.eliasnogueira.paymentservice.dto.PaymentRequest;
import com.eliasnogueira.paymentservice.model.Payment;
import com.eliasnogueira.paymentservice.model.enums.PaymentSource;
import com.eliasnogueira.paymentservice.model.enums.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
public final class PaymentDataFactory {

    private static final long MIN_VALID = 1L;
    private static final long MIN_INVALID = 2001L;
    private static final long MAX_VALID = 2000L;
    private static final long MAX_INVALID = 5000L;
    private static final int DECIMALS = 2;

    private static Faker faker = new Faker();

    private PaymentDataFactory() {
    }

    public static PaymentRequest validPaymentRequest() {
        var paymentRequest = basePaymentRequest();

        log.info("Valid PaymentRequest created: {}", paymentRequest);

        return paymentRequest;
    }

    public static PaymentRequest invalidPaymentRequest() {
        var paymentRequest = basePaymentRequest();
        paymentRequest.setAmount(BigDecimal.valueOf(faker.number().randomDouble(DECIMALS, MIN_INVALID, MAX_INVALID)));

        log.info("Invalid PaymentRequest created: {}", paymentRequest);

        return paymentRequest;
    }

    public static Payment validPayment() {
        var payment = Payment.builder()
                .payerId(UUID.randomUUID())
                .paymentSource(faker.options().option(PaymentSource.class))
                .amount(BigDecimal.valueOf(faker.number().randomDouble(DECIMALS, MIN_VALID, MAX_VALID)))
                .status(PaymentStatus.PENDING)
                .build();

        log.info("Valid Payment created: {}", payment);

        return payment;
    }

    public static Payment validPaymentWithAPayer(UUID payerId) {
        var payment = Payment.builder()
                .payerId(payerId)
                .paymentSource(faker.options().option(PaymentSource.class))
                .amount(BigDecimal.valueOf(faker.number().randomDouble(DECIMALS, MIN_VALID, MAX_VALID)))
                .status(PaymentStatus.PENDING)
                .build();

        log.info("Valid Payment with defined payer created: {}", payment);

        return payment;
    }

    private static PaymentRequest basePaymentRequest() {
        // the base one is always valid
        var paymentRequest = PaymentRequest.builder()
                .payerId(UUID.randomUUID())
                .paymentSource(faker.options().option(PaymentSource.class))
                .amount(BigDecimal.valueOf(faker.number().randomDouble(DECIMALS, MIN_VALID, MAX_VALID)))
                .build();

        return paymentRequest;
    }
}
