package com.dkghosh.orderservice.controller;

import com.dkghosh.orderservice.dto.OrderRequest;
import com.dkghosh.orderservice.dto.OrderResponse;
import com.dkghosh.orderservice.exception.OrderNotFoundException;
import com.dkghosh.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void resetMocks() {
        Mockito.reset(orderService);
    }

    @Test
    void createOrder_shouldReturnOrderResponse() throws Exception {

        OrderRequest request = new OrderRequest(
                "BOOK-123",
                2,
                new BigDecimal("49.99"),
                "1"
        );

        OrderResponse response = new OrderResponse(
                1L,
                "BOOK-123",
                2,
                new BigDecimal("49.99"),
                new BigDecimal("99.98"),
                "1",
                "CREATED"
        );

        when(orderService.createOrder(any(OrderRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.productCode").value("BOOK-123"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.unitPrice").value(49.99))
                .andExpect(jsonPath("$.totalAmount").value(99.98))
                .andExpect(jsonPath("$.customerId").value("1"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void getOrder_shouldReturnResponse_whenValidIdProvided() throws Exception {

        OrderResponse response = new OrderResponse(
                1L,
                "BOOK-123",
                2,
                new BigDecimal("49.99"),
                new BigDecimal("99.98"),
                "1",
                "CREATED"
        );

        when(orderService.getOrder(1L)).thenReturn(response);

        mockMvc.perform(get("/api/orders/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.productCode").value("BOOK-123"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.unitPrice").value(49.99))
                .andExpect(jsonPath("$.totalAmount").value(99.98))
                .andExpect(jsonPath("$.customerId").value("1"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }


    @Test
    void getOrders_shouldReturnResponse() throws Exception {

        List<OrderResponse> response = Arrays.asList(
                new OrderResponse(
                        1L,
                        "BOOK-123",
                        2,
                        new BigDecimal("49.99"),
                        new BigDecimal("99.98"),
                        "1",
                        "CREATED"
                ),
                new OrderResponse(
                        2L,
                        "LAPTOP-456",
                        2,
                        new BigDecimal("999.99"),
                        new BigDecimal("1999.98"),
                        "2",
                        "CREATED"
                )
        );

        when(orderService.getAllOrders()).thenReturn(response);

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))

                .andExpect(jsonPath("$[0].orderId").value(1))
                .andExpect(jsonPath("$[0].productCode").value("BOOK-123"))
                .andExpect(jsonPath("$[0].quantity").value(2))
                .andExpect(jsonPath("$[0].unitPrice").value(49.99))
                .andExpect(jsonPath("$[0].totalAmount").value(99.98))
                .andExpect(jsonPath("$[0].customerId").value("1"))
                .andExpect(jsonPath("$[0].status").value("CREATED"))

                .andExpect(jsonPath("$[1].orderId").value(2))
                .andExpect(jsonPath("$[1].productCode").value("LAPTOP-456"))
                .andExpect(jsonPath("$[1].quantity").value(2))
                .andExpect(jsonPath("$[1].unitPrice").value(999.99))
                .andExpect(jsonPath("$[1].totalAmount").value(1999.98))
                .andExpect(jsonPath("$[1].customerId").value("2"))
                .andExpect(jsonPath("$[1].status").value("CREATED"));

        verify(orderService).getAllOrders();
    }

    @Test
    void getOrders_shouldReturnEmptyList_whenNoOrdersExist() throws Exception {

        when(orderService.getAllOrders()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(orderService).getAllOrders();
    }

    @Test
    void getOrders_shouldReturnInternalServerError_whenServiceThrowsException() throws Exception {

        when(orderService.getAllOrders()).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isInternalServerError());

        verify(orderService).getAllOrders();
    }

    @Test
    void getOrder_shouldReturnNotFound_whenOrderDoesNotExist() throws Exception {

        when(orderService.getOrder(1L))
                .thenThrow(new OrderNotFoundException(1L));

        mockMvc.perform(get("/api/orders/{id}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Order not found with id: 1"))
                .andExpect(jsonPath("$.status").value(404));

        verify(orderService).getOrder(1L);
    }

    @Test
    void createOrder_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {

        String invalidRequest = """
            {
              "productCode": "",
              "quantity": 0,
              "unitPrice": 49.99,
              "customerId": ""
            }
            """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any());
    }

    @Test
    void createOrder_shouldReturnBadRequest_whenProductIsInvalid() throws Exception {

        String invalidRequest = """
            {
              "productCode": "",
              "quantity": 2,
              "unitPrice": 49.99,
              "customerId": "1"
            }
            """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any());
    }


    @Test
    void createOrder_shouldReturnBadRequest_whenQuantityIsInvalid() throws Exception {

        String invalidRequest = """
            {
              "productCode": "BOOK-123",
              "quantity": 0,
              "unitPrice": 49.99,
              "customerId": "1"
            }
            """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any());
    }

    @Test
    void createOrder_shouldReturnBadRequest_whenCustomerIdIsInvalid() throws Exception {

        String invalidRequest = """
        {
          "productCode": "BOOK-123",
          "quantity": 2,
          "unitPrice": 49.99,
          "customerId": ""
        }
        """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any());
    }

    @Test
    void createOrder_shouldReturnBadRequest_whenUnitPriceIsInvalid() throws Exception {

        String invalidRequest = """
        {
          "productCode": "BOOK-123",
          "quantity": 2,
          "unitPrice": 0,
          "customerId": "1"
        }
        """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any());
    }
}