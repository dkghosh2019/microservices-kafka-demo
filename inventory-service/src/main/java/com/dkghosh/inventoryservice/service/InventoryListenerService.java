package com.dkghosh.inventoryservice.service;

import com.dkghosh.inventoryservice.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class InventoryListenerService {

    private static final Logger log = LoggerFactory.getLogger(InventoryListenerService.class);

    @KafkaListener(topics = "order-created-topic", groupId = "inventory-service-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Inventory received orderId={} productCode={} quantity={}",
                event.orderId(), event.productCode(), event.quantity());

        // Starter logic only.
        // Later you can add inventory table, stock checks, and inventory-reserved events.
        log.info("Inventory reserved successfully for orderId={}", event.orderId());
    }
}
