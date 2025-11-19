package com.eliasnogueira.paymentservice.unit;

import com.eliasnogueira.paymentservice.data.PaymentDataFactory;
import com.eliasnogueira.paymentservice.dto.PaymentRequest;
import com.eliasnogueira.paymentservice.exceptions.PaymentLimitException;
import com.eliasnogueira.paymentservice.model.enums.PaymentSource;
import com.eliasnogueira.paymentservice.repository.PaymentRepository;
import com.eliasnogueira.paymentservice.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
}
