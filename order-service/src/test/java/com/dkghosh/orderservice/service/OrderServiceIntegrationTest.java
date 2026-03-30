package com.dkghosh.orderservice.service;

import com.dkghosh.orderservice.dto.OrderRequest;
import com.dkghosh.orderservice.dto.OrderResponse;
import com.dkghosh.orderservice.entity.OrderEntity;
import com.dkghosh.orderservice.event.OrderCreatedEvent;
import com.dkghosh.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    private KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("createOrder should persist order and publish Kafka event")
    void createOrder_shouldPersistOrderAndPublishKafkaEvent() {
        orderRepository.deleteAll();

        OrderRequest request = new OrderRequest(
                "BOOK-123",
                2,
                new BigDecimal("49.99"),
                "1"
        );

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertNotNull(response.orderId());
        assertEquals("BOOK-123", response.productCode());
        assertEquals(2, response.quantity());
        assertEquals(new BigDecimal("49.99"), response.unitPrice());
        assertEquals(new BigDecimal("99.98"), response.totalAmount());
        assertEquals("1", response.customerId());
        assertEquals("CREATED", response.status());

        List<OrderEntity> savedOrders = orderRepository.findAll();
        assertEquals(1, savedOrders.size());

        OrderEntity savedOrder = savedOrders.get(0);
        assertNotNull(savedOrder.getId());
        assertEquals("BOOK-123", savedOrder.getProductCode());
        assertEquals(2, savedOrder.getQuantity());
        assertEquals(new BigDecimal("49.99"), savedOrder.getUnitPrice());
        assertEquals(new BigDecimal("99.98"), savedOrder.getTotalAmount());
        assertEquals("1", savedOrder.getCustomerId());
        assertEquals("CREATED", savedOrder.getStatus().name());
        assertNotNull(savedOrder.getCreatedAt());

        verify(kafkaTemplate, times(1)).send(
                eq("order-created-topic"),
                eq("1"),
                any(OrderCreatedEvent.class)
        );
    }
}