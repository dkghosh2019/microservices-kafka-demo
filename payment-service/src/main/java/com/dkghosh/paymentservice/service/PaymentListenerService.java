package com.dkghosh.paymentservice.service;

import com.dkghosh.paymentservice.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class PaymentListenerService {

    private static final Logger log = LoggerFactory.getLogger(PaymentListenerService.class);

    @KafkaListener(topics = "order-created-topic", groupId = "payment-service-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Payment received orderId={} totalAmount={} customerId={}",
                event.orderId(), event.totalAmount(), event.customerId());

        // Starter logic only.
        // Later you can add payment validation, payment persistence, and payment-completed events.
        log.info("Payment processed successfully for orderId={}", event.orderId());
    }
}
