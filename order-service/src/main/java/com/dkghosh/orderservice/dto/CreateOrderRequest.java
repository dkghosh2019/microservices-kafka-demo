package com.dkghosh.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record CreateOrderRequest(
        @NotBlank String productCode,
        @Min(1) int quantity,
        @Min(1) BigDecimal unitPrice,
        @NotBlank String customerId
) {
}
