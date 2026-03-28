package com.dkghosh.orderservice.service;

import com.dkghosh.orderservice.dto.CreateOrderRequest;
import com.dkghosh.orderservice.dto.OrderResponse;
import com.dkghosh.orderservice.entity.OrderEntity;
import com.dkghosh.orderservice.entity.OrderStatus;
import com.dkghosh.orderservice.event.OrderCreatedEvent;
import com.dkghosh.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_shouldSaveOrderCalculateTotalAndPublishEvent() {
        CreateOrderRequest request = new CreateOrderRequest(
                "BOOK-123",
                2,
                new BigDecimal("49.99"),
                "1"
        );

        OrderEntity savedEntity = new OrderEntity();
        savedEntity.setId(1L);
        savedEntity.setCustomerId("1");
        savedEntity.setProductCode("BOOK-123");
        savedEntity.setQuantity(2);
        savedEntity.setUnitPrice(new BigDecimal("49.99"));
        savedEntity.setTotalAmount(new BigDecimal("99.98"));
        savedEntity.setStatus(OrderStatus.CREATED);

        when(orderRepository.save(any(OrderEntity.class))).thenReturn(savedEntity);

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals(1L, response.orderId());
        assertEquals("BOOK-123", response.productCode());
        assertEquals(2, response.quantity());
        assertEquals(new BigDecimal("49.99"), response.unitPrice());
        assertEquals(new BigDecimal("99.98"), response.totalAmount());
        assertEquals("1", response.customerId());
        assertEquals("CREATED", response.status());

        ArgumentCaptor<OrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);

        verify(kafkaTemplate, times(1)).send(
                eq("order-created-topic"),
                eq("1"),
                eventCaptor.capture()
        );

        OrderCreatedEvent publishedEvent = eventCaptor.getValue();
        assertEquals(1L, publishedEvent.orderId());
        assertEquals("BOOK-123", publishedEvent.productCode());
        assertEquals(2, publishedEvent.quantity());
        assertEquals(new BigDecimal("49.99"), publishedEvent.unitPrice());
        assertEquals(new BigDecimal("99.98"), publishedEvent.totalAmount());
        assertEquals("1", publishedEvent.customerId());

//
//        ArgumentCaptor<OrderEntity> entityCaptor = ArgumentCaptor.forClass(OrderEntity.class);
//        verify(orderRepository, times(1)).save(entityCaptor.capture());
//
//        OrderEntity entityToSave = entityCaptor.getValue();
//        assertEquals("1", entityToSave.getCustomerId());
//        assertEquals("BOOK-123", entityToSave.getProductCode());
//        assertEquals(2, entityToSave.getQuantity());
//        assertEquals(new BigDecimal("49.99"), entityToSave.getUnitPrice());
//        assertEquals(new BigDecimal("99.98"), entityToSave.getTotalAmount());
//
//        verify(kafkaTemplate, times(1)).send(anyString(), any(OrderCreatedEvent.class));




    }
}