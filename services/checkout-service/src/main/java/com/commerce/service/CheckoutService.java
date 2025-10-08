package com.commerce.service;

import com.commerce.dto.CheckoutRequest;
import com.commerce.dto.CheckoutResponse;
import com.commerce.dto.CheckoutItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-performance checkout service with SAGA orchestration.
 * Implements distributed transaction pattern for e-commerce checkout.
 */
@Service
public class CheckoutService {

    private static final Logger logger = LoggerFactory.getLogger(CheckoutService.class);

    @Value("${INVENTORY_SERVICE_URL:http://localhost:8081}")
    private String inventoryServiceUrl;

    @Value("${ORDER_SERVICE_URL:http://localhost:8082}")
    private String orderServiceUrl;

    @Value("${PAYMENT_SERVICE_URL:http://localhost:8083}")
    private String paymentServiceUrl;

    private final RestTemplate restTemplate;
    private final Map<UUID, Map<String, Object>> checkoutStatuses = new ConcurrentHashMap<>();

    @Autowired
    public CheckoutService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Process checkout with SAGA orchestration.
     * Target: p95 < 300ms, p99 < 800ms
     */
    public CheckoutResponse processCheckout(CheckoutRequest request) {
        long startTime = System.currentTimeMillis();
        UUID checkoutId = UUID.randomUUID();
        
        logger.info("Starting checkout process: checkoutId={}, customerId={}", 
                   checkoutId, request.getCustomerId());

        try {
            // Initialize checkout status
            Map<String, Object> status = new HashMap<>();
            status.put("status", "PROCESSING");
            status.put("step", "VALIDATION");
            status.put("startTime", startTime);
            checkoutStatuses.put(checkoutId, status);

            // Step 1: Validate and reserve inventory (parallel)
            CompletableFuture<Boolean> inventoryReservation = reserveInventoryAsync(request, checkoutId);
            
            // Step 2: Calculate total amount
            BigDecimal totalAmount = calculateTotalAmount(request);
            
            // Wait for inventory reservation
            boolean inventoryReserved = inventoryReservation.get();
            if (!inventoryReserved) {
                updateCheckoutStatus(checkoutId, "FAILED", "INVENTORY_RESERVATION_FAILED");
                return CheckoutResponse.failure("Insufficient inventory");
            }

            updateCheckoutStatus(checkoutId, "PROCESSING", "CREATING_ORDER");

            // Step 3: Create order
            UUID orderId = createOrder(request, totalAmount);
            if (orderId == null) {
                // Compensate: Release inventory
                releaseInventory(request, checkoutId);
                updateCheckoutStatus(checkoutId, "FAILED", "ORDER_CREATION_FAILED");
                return CheckoutResponse.failure("Failed to create order");
            }

            updateCheckoutStatus(checkoutId, "PROCESSING", "PROCESSING_PAYMENT");

            // Step 4: Process payment
            boolean paymentProcessed = processPayment(orderId, totalAmount, request.getPaymentMethod());
            if (!paymentProcessed) {
                // Compensate: Cancel order and release inventory
                cancelOrder(orderId);
                releaseInventory(request, checkoutId);
                updateCheckoutStatus(checkoutId, "FAILED", "PAYMENT_FAILED");
                return CheckoutResponse.failure("Payment processing failed");
            }

            // Step 5: Confirm inventory allocation
            confirmInventoryAllocation(request, orderId);

            updateCheckoutStatus(checkoutId, "COMPLETED", "SUCCESS");

            long processingTime = System.currentTimeMillis() - startTime;
            logger.info("Checkout completed successfully: checkoutId={}, orderId={}, processingTime={}ms", 
                       checkoutId, orderId, processingTime);

            CheckoutResponse response = CheckoutResponse.success(orderId, checkoutId, totalAmount, "COMPLETED");
            response.setProcessingTimeMs(processingTime);
            return response;

        } catch (Exception e) {
            logger.error("Checkout failed: checkoutId={}", checkoutId, e);
            updateCheckoutStatus(checkoutId, "FAILED", "SYSTEM_ERROR");
            
            // Best effort compensation
            try {
                releaseInventory(request, checkoutId);
            } catch (Exception compensationError) {
                logger.error("Compensation failed for checkoutId={}", checkoutId, compensationError);
            }

            return CheckoutResponse.failure("System error during checkout");
        }
    }

    /**
     * Reserve inventory asynchronously for better performance.
     */
    private CompletableFuture<Boolean> reserveInventoryAsync(CheckoutRequest request, UUID checkoutId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                for (CheckoutItem item : request.getItems()) {
                    Map<String, Object> reservationRequest = Map.of(
                        "productId", item.getProductId(),
                        "quantity", item.getQuantity(),
                        "orderId", checkoutId
                    );

                    ResponseEntity<Map> response = restTemplate.postForEntity(
                        inventoryServiceUrl + "/api/v1/inventory/reserve",
                        reservationRequest,
                        Map.class
                    );

                    if (!response.getStatusCode().is2xxSuccessful() || 
                        !Boolean.TRUE.equals(response.getBody().get("success"))) {
                        logger.warn("Failed to reserve inventory for product: {}", item.getProductId());
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                logger.error("Error during inventory reservation", e);
                return false;
            }
        });
    }

    /**
     * Calculate total amount for the order.
     */
    private BigDecimal calculateTotalAmount(CheckoutRequest request) {
        // Simplified calculation - in real implementation, would fetch product prices
        return BigDecimal.valueOf(request.getItems().size() * 100.0); // $100 per item for demo
    }

    /**
     * Create order in order service.
     */
    private UUID createOrder(CheckoutRequest request, BigDecimal totalAmount) {
        try {
            Map<String, Object> orderRequest = Map.of(
                "customerId", request.getCustomerId(),
                "items", request.getItems(),
                "totalAmount", totalAmount,
                "status", "PENDING"
            );

            ResponseEntity<Map> response = restTemplate.postForEntity(
                orderServiceUrl + "/api/v1/orders",
                orderRequest,
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return UUID.fromString(response.getBody().get("orderId").toString());
            }
        } catch (Exception e) {
            logger.error("Error creating order", e);
        }
        return null;
    }

    /**
     * Process payment.
     */
    private boolean processPayment(UUID orderId, BigDecimal amount, String paymentMethod) {
        try {
            Map<String, Object> paymentRequest = Map.of(
                "orderId", orderId,
                "amount", amount,
                "currency", "USD",
                "paymentMethod", paymentMethod
            );

            ResponseEntity<Map> resp = restTemplate.postForEntity(
                paymentServiceUrl + "/api/v1/payments/process",
                paymentRequest,
                Map.class
            );

            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                return false;
            }

            String status = String.valueOf(resp.getBody().get("status"));
            if (!"APPROVED".equalsIgnoreCase(status)) {
                return false;
            }

            // Optional: audit/order record
            String paymentId = String.valueOf(resp.getBody().get("paymentId"));
            Object echoedAmount = resp.getBody().get("amount");
            // log.debug("Payment approved id={}, amount={}", paymentId, echoedAmount);

            return true;
        } catch (org.springframework.web.client.RestClientException ex) {
            // log.error("Payment call failed", ex);
            return false;
        }
    }

    /**
     * Confirm inventory allocation.
     */
    private void confirmInventoryAllocation(CheckoutRequest request, UUID orderId) {
        for (CheckoutItem item : request.getItems()) {
            try {
                String url = String.format("%s/api/v1/inventory/confirm?productId=%s&quantity=%d&orderId=%s",
                    inventoryServiceUrl, item.getProductId(), item.getQuantity(), orderId);
                
                restTemplate.postForEntity(url, null, Map.class);
            } catch (Exception e) {
                logger.error("Error confirming inventory allocation for product: {}", item.getProductId(), e);
            }
        }
    }

    /**
     * Release inventory (compensation).
     */
    private void releaseInventory(CheckoutRequest request, UUID checkoutId) {
        for (CheckoutItem item : request.getItems()) {
            try {
                String url = String.format("%s/api/v1/inventory/release?productId=%s&quantity=%d&orderId=%s",
                    inventoryServiceUrl, item.getProductId(), item.getQuantity(), checkoutId);
                
                restTemplate.postForEntity(url, null, Map.class);
            } catch (Exception e) {
                logger.error("Error releasing inventory for product: {}", item.getProductId(), e);
            }
        }
    }

    /**
     * Cancel order (compensation).
     */
    private void cancelOrder(UUID orderId) {
        try {
            restTemplate.delete(orderServiceUrl + "/api/v1/orders/" + orderId);
        } catch (Exception e) {
            logger.error("Error canceling order: {}", orderId, e);
        }
    }

    /**
     * Update checkout status.
     */
    private void updateCheckoutStatus(UUID checkoutId, String status, String step) {
        Map<String, Object> statusMap = checkoutStatuses.get(checkoutId);
        if (statusMap != null) {
            statusMap.put("status", status);
            statusMap.put("step", step);
            statusMap.put("lastUpdated", System.currentTimeMillis());
        }
    }

    /**
     * Get checkout status.
     */
    public Map<String, Object> getCheckoutStatus(UUID checkoutId) {
        return checkoutStatuses.getOrDefault(checkoutId, Map.of("status", "NOT_FOUND"));
    }
}
