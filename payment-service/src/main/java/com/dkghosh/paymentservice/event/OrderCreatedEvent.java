package com.dkghosh.paymentservice.event;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderCreatedEvent(
        Long orderId,
        String productCode,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        String customerId,
        Instant createdAt
) {
}
