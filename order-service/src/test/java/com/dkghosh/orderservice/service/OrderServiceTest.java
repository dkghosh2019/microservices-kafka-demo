package com.dkghosh.orderservice.service;

import com.dkghosh.orderservice.config.KafkaTopicConfig;
import com.dkghosh.orderservice.dto.OrderRequest;
import com.dkghosh.orderservice.dto.OrderResponse;
import com.dkghosh.orderservice.entity.OrderEntity;
import com.dkghosh.orderservice.entity.OrderStatus;
import com.dkghosh.orderservice.event.OrderCreatedEvent;
import com.dkghosh.orderservice.exception.OrderNotFoundException;
import com.dkghosh.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
        OrderRequest request = new OrderRequest(
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
                eq(KafkaTopicConfig.ORDER_CREATED_TOPIC),
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

    @Test
    void getOrder_shouldReturnMappedOrder() {
        OrderEntity entity = new OrderEntity();
        entity.setId(10L);
        entity.setCustomerId("C-1");
        entity.setProductCode("P-1");
        entity.setQuantity(3);
        entity.setUnitPrice(new BigDecimal("15.50"));
        entity.setTotalAmount(new BigDecimal("46.50"));
        entity.setStatus(OrderStatus.CREATED);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(entity));

        OrderResponse response = orderService.getOrder(10L);

        assertNotNull(response);
        assertEquals(10L, response.orderId());
        assertEquals("P-1", response.productCode());
        assertEquals(3, response.quantity());
        assertEquals(new BigDecimal("15.50"), response.unitPrice());
        assertEquals(new BigDecimal("46.50"), response.totalAmount());
        assertEquals("C-1", response.customerId());
        assertEquals("CREATED", response.status());
    }

    @Test
    void getOrder_shouldThrowWhenMissing() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getOrder(99L));
    }

    @Test
    void getAllOrders_shouldReturnMappedOrders() {
        OrderEntity first = new OrderEntity();
        first.setId(1L);
        first.setCustomerId("C-1");
        first.setProductCode("P-1");
        first.setQuantity(1);
        first.setUnitPrice(new BigDecimal("10.00"));
        first.setTotalAmount(new BigDecimal("10.00"));
        first.setStatus(OrderStatus.CREATED);

        OrderEntity second = new OrderEntity();
        second.setId(2L);
        second.setCustomerId("C-2");
        second.setProductCode("P-2");
        second.setQuantity(2);
        second.setUnitPrice(new BigDecimal("7.50"));
        second.setTotalAmount(new BigDecimal("15.00"));
        second.setStatus(OrderStatus.CREATED);

        when(orderRepository.findAll()).thenReturn(List.of(first, second));

        List<OrderResponse> responses = orderService.getAllOrders();

        assertEquals(2, responses.size());

        OrderResponse firstResponse = responses.get(0);
        assertEquals(1L, firstResponse.orderId());
        assertEquals("P-1", firstResponse.productCode());
        assertEquals(1, firstResponse.quantity());
        assertEquals(new BigDecimal("10.00"), firstResponse.unitPrice());
        assertEquals(new BigDecimal("10.00"), firstResponse.totalAmount());
        assertEquals("C-1", firstResponse.customerId());
        assertEquals("CREATED", firstResponse.status());

        OrderResponse secondResponse = responses.get(1);
        assertEquals(2L, secondResponse.orderId());
        assertEquals("P-2", secondResponse.productCode());
        assertEquals(2, secondResponse.quantity());
        assertEquals(new BigDecimal("7.50"), secondResponse.unitPrice());
        assertEquals(new BigDecimal("15.00"), secondResponse.totalAmount());
        assertEquals("C-2", secondResponse.customerId());
        assertEquals("CREATED", secondResponse.status());
    }
}
