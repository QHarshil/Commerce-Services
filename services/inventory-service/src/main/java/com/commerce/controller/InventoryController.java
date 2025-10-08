package com.commerce.controller;

import com.commerce.dto.InventoryResponse;
import com.commerce.dto.StockReservationRequest;
import com.commerce.service.InventoryService;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for inventory operations.
 * Optimized for high-throughput e-commerce scenarios.
 */
@RestController
@RequestMapping("/api/v1/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Get all products with inventory.
     */
    @GetMapping("/products")
    @Timed(value = "inventory.get_all", description = "Time taken to get all products")
    public ResponseEntity<List<InventoryResponse>> getAllProducts() {
        logger.debug("Getting all products with inventory");
        List<InventoryResponse> products = inventoryService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Get inventory for a specific product.
     */
    @GetMapping("/products/{productId}")
    @Timed(value = "inventory.get", description = "Time taken to get inventory")
    public ResponseEntity<?> getInventory(@PathVariable UUID productId) {
        logger.debug("Getting inventory for product: {}", productId);
        
        return inventoryService.getInventory(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get inventory by product SKU.
     */
    @GetMapping("/sku/{sku}")
    @Timed(value = "inventory.get.sku", description = "Time taken to get inventory by SKU")
    public ResponseEntity<?> getInventoryBySku(@PathVariable String sku) {
        logger.debug("Getting inventory for SKU: {}", sku);
        
        return inventoryService.getInventoryBySku(sku)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Check stock availability.
     */
    @GetMapping("/availability/{productId}")
    @Timed(value = "inventory.check.availability", description = "Time taken to check stock availability")
    public ResponseEntity<Map<String, Object>> checkAvailability(
            @PathVariable UUID productId,
            @RequestParam Integer quantity) {
        
        logger.debug("Checking availability for product: {}, quantity: {}", productId, quantity);
        
        boolean available = inventoryService.isStockAvailable(productId, quantity);
        
        return ResponseEntity.ok(Map.of(
            "productId", productId,
            "requestedQuantity", quantity,
            "available", available
        ));
    }

    /**
     * Reserve stock for an order.
     */
    @PostMapping("/reserve")
    @Timed(value = "inventory.reserve", description = "Time taken to reserve stock")
    public ResponseEntity<Map<String, Object>> reserveStock(@Valid @RequestBody StockReservationRequest request) {
        logger.info("Reserving stock: {}", request);
        
        boolean success = inventoryService.reserveStock(request);
        
        if (success) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Stock reserved successfully",
                "orderId", request.getOrderId(),
                "productId", request.getProductId(),
                "quantity", request.getQuantity()
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to reserve stock - insufficient quantity or product not found",
                "orderId", request.getOrderId(),
                "productId", request.getProductId(),
                "quantity", request.getQuantity()
            ));
        }
    }

    /**
     * Release reserved stock.
     */
    @PostMapping("/release")
    @Timed(value = "inventory.release", description = "Time taken to release reserved stock")
    public ResponseEntity<Map<String, Object>> releaseStock(
            @RequestParam UUID productId,
            @RequestParam Integer quantity,
            @RequestParam UUID orderId) {
        
        logger.info("Releasing stock for order: {}, product: {}, quantity: {}", orderId, productId, quantity);
        
        boolean success = inventoryService.releaseReservedStock(productId, quantity, orderId);
        
        if (success) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Reserved stock released successfully"
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to release reserved stock"
            ));
        }
    }

    /**
     * Confirm stock allocation.
     */
    @PostMapping("/confirm")
    @Timed(value = "inventory.confirm", description = "Time taken to confirm allocation")
    public ResponseEntity<Map<String, Object>> confirmAllocation(
            @RequestParam UUID productId,
            @RequestParam Integer quantity,
            @RequestParam UUID orderId) {
        
        logger.info("Confirming allocation for order: {}, product: {}, quantity: {}", orderId, productId, quantity);
        
        boolean success = inventoryService.confirmAllocation(productId, quantity, orderId);
        
        if (success) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Stock allocation confirmed successfully"
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to confirm stock allocation"
            ));
        }
    }

    /**
     * Update inventory quantity.
     */
    @PutMapping("/products/{productId}")
    @Timed(value = "inventory.update", description = "Time taken to update inventory")
    public ResponseEntity<Map<String, Object>> updateInventory(
            @PathVariable UUID productId,
            @RequestParam Integer quantity) {
        
        logger.info("Updating inventory for product: {}, quantity: {}", productId, quantity);
        
        boolean success = inventoryService.updateInventory(productId, quantity);
        
        if (success) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Inventory updated successfully"
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to update inventory"
            ));
        }
    }

    /**
     * Get low stock items.
     */
    @GetMapping("/low-stock")
    @Timed(value = "inventory.low.stock", description = "Time taken to get low stock items")
    public ResponseEntity<List<InventoryResponse>> getLowStockItems(
            @RequestParam(defaultValue = "10") Integer threshold) {
        
        logger.debug("Getting low stock items with threshold: {}", threshold);
        
        List<InventoryResponse> lowStockItems = inventoryService.getLowStockItems(threshold);
        return ResponseEntity.ok(lowStockItems);
    }

    /**
     * Get inventory by category.
     */
    @GetMapping("/category/{category}")
    @Timed(value = "inventory.category", description = "Time taken to get inventory by category")
    public ResponseEntity<List<InventoryResponse>> getInventoryByCategory(@PathVariable String category) {
        logger.debug("Getting inventory for category: {}", category);
        
        List<InventoryResponse> inventory = inventoryService.getInventoryByCategory(category);
        return ResponseEntity.ok(inventory);
    }

    /**
     * Get total inventory value.
     */
    @GetMapping("/value")
    @Timed(value = "inventory.value", description = "Time taken to calculate inventory value")
    public ResponseEntity<Map<String, Object>> getTotalInventoryValue() {
        logger.debug("Getting total inventory value");
        
        Double totalValue = inventoryService.getTotalInventoryValue();
        
        return ResponseEntity.ok(Map.of(
            "totalValue", totalValue,
            "currency", "USD"
        ));
    }
}
