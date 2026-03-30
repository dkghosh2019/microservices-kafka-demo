package com.dkghosh.orderservice.controller;

import com.dkghosh.orderservice.event.OrderCreatedEvent;
import com.dkghosh.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    private KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }


    @Test
    @DisplayName("POST /api/orders should create order, persist it, and return 201")
    void createOrder_shouldPersistOrderAndReturnCreated() throws Exception {

        String requestBody = """
                {
                  "productCode": "BOOK-123",
                  "quantity": 2,
                  "unitPrice": 49.99,
                  "customerId": "1"
                }
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.productCode", is("BOOK-123")))
                .andExpect(jsonPath("$.quantity", is(2)))
                .andExpect(jsonPath("$.unitPrice", is(49.99)))
                .andExpect(jsonPath("$.totalAmount", is(99.98)))
                .andExpect(jsonPath("$.customerId", is("1")))
                .andExpect(jsonPath("$.status", is("CREATED")));

        verify(kafkaTemplate, times(1)).send(
                eq("order-created-topic"),
                eq("1"),
                any(OrderCreatedEvent.class)
        );
    }

}