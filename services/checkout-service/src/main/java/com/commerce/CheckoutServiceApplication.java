package com.commerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Checkout Service - High-performance checkout orchestration.
 * 
 * Features:
 * - SAGA orchestration for distributed transactions
 * - Circuit breaker pattern for resilience
 * - Sub-300ms p95 latency for checkout operations
 * - Idempotency for safe retries
 */
@SpringBootApplication
@EnableKafka
@EnableTransactionManagement
public class CheckoutServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CheckoutServiceApplication.class, args);
    }
}
