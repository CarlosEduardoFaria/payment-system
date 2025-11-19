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
package com.eliasnogueira.paymentservice.integration;

import com.eliasnogueira.paymentservice.data.PaymentDataFactory;
import com.eliasnogueira.paymentservice.dto.PaymentResponse;
import com.eliasnogueira.paymentservice.dto.PaymentUpdateRequest;
import com.eliasnogueira.paymentservice.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static com.eliasnogueira.paymentservice.model.enums.PaymentStatus.PAID;
import static com.eliasnogueira.paymentservice.model.enums.PaymentStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected PaymentRepository paymentRepository;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    void cleanDatabase() {
        paymentRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create a new payment and return 201 Created")
    void createPayment() throws Exception {
        var paymentRequest = PaymentDataFactory.validPaymentRequest();

        var responseInJson = mockMvc.perform(post("/api/payments")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PaymentResponse paymentResponse = mapper.readValue(responseInJson, PaymentResponse.class);

        assertThat(paymentResponse.getId()).isNotNull();
        assertThat(paymentResponse.getPayerId()).isEqualTo(paymentRequest.getPayerId());
        assertThat(paymentResponse.getPaymentSource()).isEqualTo(paymentRequest.getPaymentSource());
        assertThat(paymentResponse.getAmount()).isEqualTo(paymentRequest.getAmount());
        assertThat(paymentResponse.getStatus()).isEqualTo(PENDING);
    }

    @Test
    @DisplayName("Should find a payment by ID and return 200 OK")
    void getPayment() throws Exception {
        var payment = PaymentDataFactory.validPayment();

        var savedPayment = paymentRepository.save(payment);

        var responseInJson = mockMvc.perform(get("/api/payments/{paymentId}",
                        savedPayment.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var paymentResponse = mapper.readValue(responseInJson, PaymentResponse.class);

        assertThat(paymentResponse.getId()).isEqualTo(savedPayment.getId());
        assertThat(paymentResponse.getPayerId()).isEqualTo(savedPayment.getPayerId());
        assertThat(paymentResponse.getPaymentSource()).isEqualTo(savedPayment.getPaymentSource());
        assertThat(paymentResponse.getAmount()).isEqualByComparingTo(savedPayment.getAmount());
        assertThat(paymentResponse.getStatus()).isEqualTo(savedPayment.getStatus());
    }

    @Test
    @DisplayName("Should find all payments and return 200 OK")
    void getAllPayments() throws Exception {
        UUID payerId = UUID.randomUUID();

        var firstPayment = PaymentDataFactory.validPaymentWithAPayer(payerId);
        var secondPayment = PaymentDataFactory.validPaymentWithAPayer(payerId);

        paymentRepository.saveAll(List.of(firstPayment, secondPayment));

        var responseInJson = mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<PaymentResponse> listOfPaymentResponse = mapper.readValue(responseInJson,
                mapper.getTypeFactory().constructCollectionType(List.class, PaymentResponse.class)
        );

        assertThat(listOfPaymentResponse).hasSize(2);
        assertThat(listOfPaymentResponse.get(0).getId()).isNotNull();
        assertThat(listOfPaymentResponse.get(0).getPayerId()).isEqualTo(payerId);
        assertThat(listOfPaymentResponse.get(0).getPaymentSource()).isEqualTo(firstPayment.getPaymentSource());
        assertThat(listOfPaymentResponse.get(0).getAmount()).isEqualByComparingTo(firstPayment.getAmount());
        assertThat(listOfPaymentResponse.get(0).getStatus()).isEqualTo(PENDING);
        assertThat(listOfPaymentResponse.get(1).getId()).isNotNull();
        assertThat(listOfPaymentResponse.get(1).getPayerId()).isEqualTo(payerId);
        assertThat(listOfPaymentResponse.get(1).getPaymentSource()).isEqualTo(secondPayment.getPaymentSource());
        assertThat(listOfPaymentResponse.get(1).getAmount()).isEqualByComparingTo(secondPayment.getAmount());
        assertThat(listOfPaymentResponse.get(1).getStatus()).isEqualTo(PENDING);
    }

    @Test
    @DisplayName("Should find all payments by payerId and return 200 OK")
    void getPaymentsByPayerId() throws Exception {
        UUID payerId = UUID.randomUUID();

        var firstPayment = PaymentDataFactory.validPaymentWithAPayer(payerId);
        var secondPayment = PaymentDataFactory.validPaymentWithAPayer(payerId);

        paymentRepository.saveAll(List.of(firstPayment, secondPayment));

        var responseInJson = mockMvc.perform(get("/api/payments/payer/{payerId}", payerId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<PaymentResponse> listOfPaymentResponse = mapper.readValue(responseInJson,
                mapper.getTypeFactory().constructCollectionType(List.class, PaymentResponse.class)
        );

        assertThat(listOfPaymentResponse).hasSize(2);
        assertThat(listOfPaymentResponse.get(0).getId()).isNotNull();
        assertThat(listOfPaymentResponse.get(0).getPayerId()).isEqualTo(payerId);
        assertThat(listOfPaymentResponse.get(0).getPaymentSource()).isEqualTo(firstPayment.getPaymentSource());
        assertThat(listOfPaymentResponse.get(0).getAmount()).isEqualByComparingTo(firstPayment.getAmount());
        assertThat(listOfPaymentResponse.get(0).getStatus()).isEqualTo(PENDING);
        assertThat(listOfPaymentResponse.get(1).getId()).isNotNull();
        assertThat(listOfPaymentResponse.get(1).getPayerId()).isEqualTo(payerId);
        assertThat(listOfPaymentResponse.get(1).getPaymentSource()).isEqualTo(secondPayment.getPaymentSource());
        assertThat(listOfPaymentResponse.get(1).getAmount()).isEqualByComparingTo(secondPayment.getAmount());
    }

    @Test
    @DisplayName("Should update a payment and return 200 OK")
    void updatePayment() throws Exception {
        var paymentUpdateRequest = PaymentUpdateRequest.builder().status(PAID).build();

        var payment = PaymentDataFactory.validPayment();

        var savedPayment = paymentRepository.save(payment);

        String responseInJson = mockMvc.perform(put("/api/payments/{paymentId}", savedPayment.getId())
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(paymentUpdateRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PaymentResponse updatedPaymentResponse = mapper.readValue(responseInJson, PaymentResponse.class);

        assertThat(updatedPaymentResponse.getId()).isEqualTo(savedPayment.getId());
        assertThat(updatedPaymentResponse.getStatus()).isEqualTo(PAID);
        assertThat(updatedPaymentResponse.getPayerId()).isEqualTo(savedPayment.getPayerId());
        assertThat(updatedPaymentResponse.getPaymentSource()).isEqualTo(savedPayment.getPaymentSource());
        assertThat(updatedPaymentResponse.getAmount()).isEqualByComparingTo(savedPayment.getAmount());
    }

    @Test
    @DisplayName("Should return 404 when payment not found for update")
    void updatePayment_ShouldReturn404WhenPaymentNotFound() throws Exception {
        var paymentUpdateRequest = PaymentUpdateRequest.builder().status(PAID).build();

        var nonExistentPaymentId = UUID.randomUUID();

        mockMvc.perform(put("/api/payments/{paymentId}/payer/{payerId}",
                        nonExistentPaymentId, "123456789")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(paymentUpdateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when payment not found")
    void getPayment_ShouldReturn404WhenNotFound() throws Exception {
        var nonExistentId = new Random().nextLong();

        mockMvc.perform(get("/api/payments/{paymentId}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return empty list when no payments exist")
    void getAllPayments_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
