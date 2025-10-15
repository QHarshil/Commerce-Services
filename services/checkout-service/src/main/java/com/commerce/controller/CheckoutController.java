package com.commerce.controller;

import com.commerce.dto.CheckoutRequest;
import com.commerce.dto.CheckoutResponse;
import com.commerce.service.CheckoutService;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;

/**
 * High-performance checkout controller.
 * Optimized for sub-300ms p95 latency.
 */
@RestController
@RequestMapping("/api/v1/checkout")
@CrossOrigin(origins = "*")
public class CheckoutController {

    private static final Logger logger = LoggerFactory.getLogger(CheckoutController.class);

    private final CheckoutService checkoutService;

    @Autowired
    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    /**
     * Process checkout with SAGA orchestration.
     * Target: p95 < 300ms, p99 < 800ms
     */
    @PostMapping("/process")
    @Timed(value = "checkout.process", description = "Time taken to process checkout")
    public ResponseEntity<CheckoutResponse> processCheckout(@Valid @RequestBody CheckoutRequest request) {
        logger.info("Processing checkout for customer: {}, items: {}", 
                   request.getCustomerId(), request.getItems().size());
        
        CheckoutResponse response = checkoutService.processCheckout(request);
        
        if (response.isSuccess()) {
            logger.info("Checkout successful: orderId={}, total={}", 
                       response.getOrderId(), response.getTotalAmount());
            return ResponseEntity.ok(response);
        } else {
            logger.warn("Checkout failed: {}", response.getErrorMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get checkout status.
     */
    @GetMapping("/status/{checkoutId}")
    @Timed(value = "checkout.status", description = "Time taken to get checkout status")
    public ResponseEntity<Map<String, Object>> getCheckoutStatus(@PathVariable UUID checkoutId) {
        logger.debug("Getting checkout status for: {}", checkoutId);
        
        Map<String, Object> status = checkoutService.getCheckoutStatus(checkoutId);
        return ResponseEntity.ok(status);
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "checkout-service",
            "timestamp", System.currentTimeMillis()
        ));
    }
}
