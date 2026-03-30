package com.dkghosh.orderservice.service;

import com.dkghosh.orderservice.config.KafkaTopicConfig;
import com.dkghosh.orderservice.dto.OrderRequest;
import com.dkghosh.orderservice.dto.OrderResponse;
import com.dkghosh.orderservice.entity.OrderEntity;
import com.dkghosh.orderservice.entity.OrderStatus;
import com.dkghosh.orderservice.event.OrderCreatedEvent;
import com.dkghosh.orderservice.exception.OrderNotFoundException;
import com.dkghosh.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderService(OrderRepository orderRepository,
                        KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        OrderEntity order = new OrderEntity();
        order.setProductCode(request.productCode());
        order.setQuantity(request.quantity());
        order.setUnitPrice(request.unitPrice());
        order.setCustomerId(request.customerId());
        order.setTotalAmount(request.unitPrice().multiply(java.math.BigDecimal.valueOf(request.quantity())));
        order.setStatus(OrderStatus.CREATED);

        OrderEntity saved = orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(
                saved.getId(),
                saved.getProductCode(),
                saved.getQuantity(),
                saved.getUnitPrice(),
                saved.getTotalAmount(),
                saved.getCustomerId(),
                saved.getCreatedAt()
        );

        kafkaTemplate.send(KafkaTopicConfig.ORDER_CREATED_TOPIC, String.valueOf(saved.getId()), event);
        log.info("Published OrderCreatedEvent for orderId={}", saved.getId());

        return mapToResponse(saved);
    }

    public OrderResponse getOrder(Long id) {
        return orderRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    private OrderResponse mapToResponse(OrderEntity order) {
        return new OrderResponse(
                order.getId(),
                order.getProductCode(),
                order.getQuantity(),
                order.getUnitPrice(),
                order.getTotalAmount(),
                order.getCustomerId(),
                order.getStatus().name()
        );
    }
}
