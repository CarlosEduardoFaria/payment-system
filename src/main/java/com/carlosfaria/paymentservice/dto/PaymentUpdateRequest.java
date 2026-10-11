package com.carlosfaria.paymentservice.dto;

import com.carlosfaria.paymentservice.model.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PaymentUpdateRequest {

    @NotNull(message = "Status is required")
    private PaymentStatus status;
}
