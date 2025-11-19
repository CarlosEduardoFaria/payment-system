/*
 * MIT License
 *
 * Copyright (c) 2025 Elias Nogueira
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.eliasnogueira.paymentservice.service;

import com.eliasnogueira.paymentservice.dto.PaymentRequest;
import com.eliasnogueira.paymentservice.dto.PaymentResponse;
import com.eliasnogueira.paymentservice.dto.PaymentUpdateRequest;
import com.eliasnogueira.paymentservice.exceptions.PaymentLimitException;
import com.eliasnogueira.paymentservice.exceptions.PaymentNotFoundException;
import com.eliasnogueira.paymentservice.model.Payment;
import com.eliasnogueira.paymentservice.model.enums.PaymentStatus;
import com.eliasnogueira.paymentservice.repository.PaymentRepository;
import com.eliasnogueira.paymentservice.validator.PaymentLimitValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentResponse createPayment(PaymentRequest paymentRequest) {
        checkDailyLimit(paymentRequest);

        var payment = Payment.builder()
                .payerId(paymentRequest.getPayerId())
                .paymentSource(paymentRequest.getPaymentSource())
                .amount(paymentRequest.getAmount())
                .status(PaymentStatus.PENDING)
                .build();

        var savedPayment = paymentRepository.save(payment);
        log.info("Payment created with ID: {}", savedPayment.getId());

        return new ModelMapper().map(savedPayment, PaymentResponse.class);
    }

    @Transactional
    public PaymentResponse updatePayment(Long paymentId, PaymentUpdateRequest updateRequest) {
        var payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));

        var newStatus = updateRequest.getStatus();

        payment.setStatus(newStatus);
        var updatedPayment = paymentRepository.save(payment);
        log.info("Payment updated with ID: {}, new status: {}", paymentId, newStatus);

        return new ModelMapper().map(updatedPayment, PaymentResponse.class);
    }

    public PaymentResponse getPaymentById(Long paymentId) {
        var payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));
        return new ModelMapper().map(payment, PaymentResponse.class);
    }

    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(payment -> new ModelMapper().map(payment, PaymentResponse.class)).toList();
    }

    public List<PaymentResponse> getPaymentsByPayerId(UUID payerId) {
        return paymentRepository.findAllByPayerId(payerId).stream()
                .map(payment -> new ModelMapper().map(payment, PaymentResponse.class)).toList();
    }

    private void checkDailyLimit(PaymentRequest paymentRequest) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        BigDecimal dailyTotal = paymentRepository.sumPaymentsByPayerIdAndDate(paymentRequest.getPayerId(), startOfDay, endOfDay);

        if (dailyTotal == null) {
            dailyTotal = BigDecimal.ZERO;
        }

        BigDecimal novoTotal = dailyTotal.add(paymentRequest.getAmount());

        if (!PaymentLimitValidator.isWithinLimit(novoTotal)) {
            throw new PaymentLimitException("Daily payment limit exceeded for source: " + paymentRequest.getPaymentSource());
        }
    }
}