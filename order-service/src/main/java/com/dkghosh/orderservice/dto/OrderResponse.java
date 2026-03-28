package com.dkghosh.orderservice.dto;

import java.math.BigDecimal;

public record OrderResponse(
        Long orderId,
        String productCode,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        String customerId,
        String status
) {
}
