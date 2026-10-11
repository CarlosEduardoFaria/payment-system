package com.carlosfaria.paymentservice.unit;

import com.carlosfaria.paymentservice.data.PaymentDataFactory;
import com.carlosfaria.paymentservice.dto.PaymentRequest;
import com.carlosfaria.paymentservice.exceptions.PaymentLimitException;
import com.carlosfaria.paymentservice.model.enums.PaymentSource;
import com.carlosfaria.paymentservice.repository.PaymentRepository;
import com.carlosfaria.paymentservice.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("Should save the payment when daily limit is not reached")
    void shouldSavePaymentWhenAmountIsNotExceedDailyLimit() {
        when(paymentRepository.sumPaymentsByPayerIdAndDate(any(), any(), any()))
                .thenReturn(new BigDecimal("200.00"));

        when(paymentRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var paymentRequest = PaymentDataFactory.validPaymentRequest();

        var savedPayment = paymentService.createPayment(paymentRequest);

        assertThat(savedPayment.getPayerId()).isEqualTo(paymentRequest.getPayerId());
        verify(paymentRepository).save(any());
    }

    @Test
    @DisplayName("Should thrown an exception when the daily limit exceed")
    void shouldThrownExceptionWhenAmountExceedDailyLimit() {
        var paymentRequest = PaymentDataFactory.invalidPaymentRequest();

        when(paymentRepository.sumPaymentsByPayerIdAndDate(any(), any(), any()))
                .thenReturn(paymentRequest.getAmount());

        assertThatThrownBy(() -> paymentService.createPayment(paymentRequest))
                .isInstanceOf(PaymentLimitException.class)
                .hasMessageContaining("Daily payment limit exceeded");
    }

    @ParameterizedTest(name = "Should thrown an exception when amount {0}")
    @ValueSource(strings = {"0.00", "-100.99"})
    @DisplayName("Should thrown an exception when amount is less or equal than zero")
    void shouldThrownExceptionWhenAmountIsLessOrEqualZero(BigDecimal amount) {
        var paymentRequest = PaymentRequest.builder()
                .payerId(UUID.randomUUID())
                .paymentSource(PaymentSource.DEBIT_CARD)
                .amount(amount)
                .build();

        when(paymentRepository.sumPaymentsByPayerIdAndDate(any(), any(), any()))
                .thenReturn(paymentRequest.getAmount());

        assertThatThrownBy(() -> paymentService.createPayment(paymentRequest))
                .isInstanceOf(PaymentLimitException.class)
                .hasMessageContaining("Amount must be greater than zero");
    }
}
