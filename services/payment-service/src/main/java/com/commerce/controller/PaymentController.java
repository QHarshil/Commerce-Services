package com.commerce.controller;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin(origins = "http://localhost:3000")
public class PaymentController {

    @PostMapping("/authorize")
    public ResponseEntity<Map<String,Object>> authorize(@RequestBody Map<String,Object> body) {
        double amount = body.get("amount") instanceof Number ? ((Number) body.get("amount")).doubleValue() : 0.0;
        return ResponseEntity.status(201).body(Map.of(
        "paymentId", UUID.randomUUID().toString(),
        "status", "APPROVED",
        "amount", amount,
        "createdAt", Instant.now().toEpochMilli()
        ));
    }

    @PostMapping("/capture")
    public ResponseEntity<Map<String,Object>> capture(@RequestBody Map<String,Object> body) {
        double amount = body.get("amount") instanceof Number ? ((Number) body.get("amount")).doubleValue() : 0.0;
        return ResponseEntity.status(201).body(Map.of(
        "paymentId", UUID.randomUUID().toString(),
        "status", "APPROVED",
        "amount", amount,
        "createdAt", Instant.now().toEpochMilli()
        ));
    }

    @PostMapping
    public ResponseEntity<Map<String,Object>> pay(@RequestBody Map<String,Object> body) {
    double amount = body.get("amount") instanceof Number ? ((Number) body.get("amount")).doubleValue() : 0.0;
    return ResponseEntity.status(201).body(Map.of(
        "paymentId", UUID.randomUUID().toString(),
        "status", "APPROVED",
        "amount", amount,
        "createdAt", Instant.now().toEpochMilli()
        ));
    }

    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> process(@RequestBody Map<String, Object> body) {
    double amount = body.get("amount") instanceof Number ? ((Number) body.get("amount")).doubleValue() : 0.0;
    return ResponseEntity.status(201).body(Map.of(
        "paymentId", UUID.randomUUID().toString(),
        "status", "APPROVED",
        "amount", amount,
        "createdAt", Instant.now().toEpochMilli()
        ));
    }
}
