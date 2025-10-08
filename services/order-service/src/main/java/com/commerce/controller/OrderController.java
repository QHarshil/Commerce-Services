package com.commerce.controller;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@CrossOrigin(origins = "http://localhost:3000")
public class OrderController {

    public static class OrderCreateResponse {
        public UUID orderId;
        public String status = "CREATED";
        public long createdAt = Instant.now().toEpochMilli();
        public Double totalAmount;
        public OrderCreateResponse(UUID orderId, Double totalAmount) {
            this.orderId = orderId; this.totalAmount = totalAmount;
        }
    }

    @PostMapping
    public ResponseEntity<OrderCreateResponse> create(@RequestBody Map<String,Object> body) {
        Double total = null;
        Object maybeTotal = body.get("totalAmount");
        if (maybeTotal instanceof Number) total = ((Number) maybeTotal).doubleValue();
        return ResponseEntity.status(201).body(new OrderCreateResponse(UUID.randomUUID(), total));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancel(@PathVariable UUID orderId) {
        // no-op demo cancellation; return 204 so checkout proceeds
        return ResponseEntity.noContent().build();
    }
}
