package com.eliasnogueira.paymentservice.integration;

import com.eliasnogueira.paymentservice.model.Payment;
import com.eliasnogueira.paymentservice.model.enums.PaymentSource;
import com.eliasnogueira.paymentservice.model.enums.PaymentStatus;
import com.eliasnogueira.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PaymentsDatabaseIntegrationTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("Should correctly sum daily payments by payerId")
    void shouldSumDailyPaymentsByPayerId() {
        UUID payerId = UUID.randomUUID();

        var firstPayment = Payment.builder().payerId(payerId).paymentSource(PaymentSource.PIX)
                .amount(BigDecimal.valueOf(50.00)).status(PaymentStatus.PENDING).build();

        var secondPayment = Payment.builder().payerId(payerId).paymentSource(PaymentSource.CREDIT_CARD)
                .amount(BigDecimal.valueOf(350.00)).status(PaymentStatus.PENDING).build();

        paymentRepository.saveAll(List.of(firstPayment, secondPayment));

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        BigDecimal total = paymentRepository.sumPaymentsByPayerIdAndDate(payerId, startOfDay, endOfDay);

        assertThat(total).isEqualByComparingTo("400.00");
    }

    @Test
    @DisplayName("Should return zero when there are no payments for the payerId on the day")
    void shouldReturnZeroWhenNoPayments() {
        UUID nonExistentPayerId = UUID.randomUUID();
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        BigDecimal total = paymentRepository.sumPaymentsByPayerIdAndDate(nonExistentPayerId, startOfDay, endOfDay);

        assertThat(total).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Should find payments by payerId")
    void shouldFindPaymentsByPayerId() {
        UUID payerId = UUID.randomUUID();

        var firstPayment = Payment.builder().payerId(payerId).paymentSource(PaymentSource.PIX)
                .amount(BigDecimal.valueOf(50.00)).status(PaymentStatus.PENDING).build();

        var secondPayment = Payment.builder().payerId(payerId)
                .paymentSource(PaymentSource.CREDIT_CARD).amount(BigDecimal.valueOf(150.00))
                .status(PaymentStatus.PAID).build();

        paymentRepository.saveAll(List.of(firstPayment, secondPayment));

        List<Payment> payments = paymentRepository.findAllByPayerId(payerId);

        assertThat(payments).hasSize(2);
        assertThat(payments).extracting(Payment::getPayerId).containsOnly(payerId);
        assertThat(payments).extracting(Payment::getAmount)
                .contains(BigDecimal.valueOf(50.00), BigDecimal.valueOf(150.00));
    }

    @Test
    @DisplayName("Should return empty list when there are no payments for the payerId")
    void shouldReturnEmptyListForNonExistentPayerId() {
        List<Payment> payments = paymentRepository.findAllByPayerId(UUID.randomUUID());

        assertThat(payments).isEmpty();
    }
}