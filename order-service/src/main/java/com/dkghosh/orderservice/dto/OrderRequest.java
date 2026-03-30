package com.dkghosh.orderservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record OrderRequest(
        @NotBlank String productCode,
        @Min(1) int quantity,
        @DecimalMin(value = "0.01", inclusive = true) BigDecimal unitPrice,
        @NotBlank String customerId
) {
}
