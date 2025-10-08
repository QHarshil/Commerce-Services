package com.commerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Inventory Service - High-performance inventory management with optimistic locking.
 * 
 * Features:
 * - Real-time inventory tracking with Redis caching
 * - Optimistic locking for concurrent stock updates
 * - Event-driven architecture with Kafka
 * - Circuit breaker pattern for resilience
 */
@SpringBootApplication
@EnableCaching
@EnableKafka
@EnableTransactionManagement
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
