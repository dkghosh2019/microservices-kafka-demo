package com.dkghosh.inventoryservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InventoryHealthController {

    private static final Logger log = LoggerFactory.getLogger(InventoryHealthController.class);

    @GetMapping("/health")
    public String health() {
        log.info("Inventory health endpoint called");
        return "inventory-service is up";
    }
}