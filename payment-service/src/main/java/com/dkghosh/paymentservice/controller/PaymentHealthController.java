package com.dkghosh.paymentservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PaymentHealthController {

    @GetMapping("/api/payment/status")
    public Map<String, String> status() {
        return Map.of("service", "payment-service", "status", "UP");
    }
}
