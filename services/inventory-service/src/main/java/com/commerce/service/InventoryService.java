package com.commerce.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.commerce.dto.InventoryResponse;
import com.commerce.dto.StockReservationRequest;
import com.commerce.event.InventoryEvent;
import com.commerce.model.Inventory;
import com.commerce.repository.InventoryRepository;
import com.commerce.repository.ProductRepository;

/**
 * High-performance inventory service with caching and event publishing.
 */
@Service
@Transactional
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);
    private static final String INVENTORY_TOPIC = "inventory-events";

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final KafkaTemplate<String, InventoryEvent> kafkaTemplate;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepository,
                           ProductRepository productRepository,
                           KafkaTemplate<String, InventoryEvent> kafkaTemplate) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Get all products with inventory information.
     */
    @Cacheable(value = "products", key = "'all'")
    public List<InventoryResponse> getAllProducts() {
        logger.debug("Getting all products with inventory");
        
        List<Inventory> inventories = inventoryRepository.findAll();
        
        return inventories.stream()
                .map(this::mapToInventoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get inventory for a product with caching.
     */
    @Cacheable(value = "inventory", key = "#productId")
    @Transactional(readOnly = true)
    public Optional<InventoryResponse> getInventory(UUID productId) {
        logger.debug("Getting inventory for product: {}", productId);
        
        return inventoryRepository.findByProductIdWithProduct(productId)
                .map(this::mapToInventoryResponse);
    }

    /**
     * Get inventory by SKU.
     */
    @Cacheable(value = "inventory-sku", key = "#sku")
    @Transactional(readOnly = true)
    public Optional<InventoryResponse> getInventoryBySku(String sku) {
        logger.debug("Getting inventory for SKU: {}", sku);
        
        return inventoryRepository.findByProductSku(sku)
                .map(this::mapToInventoryResponse);
    }

    /**
     * Check if stock is available.
     */
    @Cacheable(value = "stock-availability", key = "#productId + '_' + #quantity")
    @Transactional(readOnly = true)
    public boolean isStockAvailable(UUID productId, Integer quantity) {
        logger.debug("Checking stock availability for product: {}, quantity: {}", productId, quantity);
        
        return inventoryRepository.findByProductIdWithProduct(productId)
                .map(inventory -> inventory.getAvailableQuantity() >= quantity)
                .orElse(false);
    }

    /**
     * Reserve stock with optimistic locking and retry.
     */
    @Retryable(value = {OptimisticLockingFailureException.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    @CacheEvict(value = {"inventory", "stock-availability", "products"}, key = "#request.productId")
    public boolean reserveStock(StockReservationRequest request) {
        return reserveStock(request.getProductId(), request.getQuantity(), request.getOrderId());
    }

    /**
     * Reserve stock with optimistic locking and retry.
     */
    @Retryable(value = {OptimisticLockingFailureException.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    @CacheEvict(value = {"inventory", "stock-availability", "products"}, allEntries = true)
    public boolean reserveStock(UUID productId, Integer quantity, UUID orderId) {
        logger.info("Reserving stock: productId={}, quantity={}, orderId={}", productId, quantity, orderId);

        try {
            Optional<Inventory> inventoryOpt = inventoryRepository.findByProductIdWithProduct(productId);
            
            if (inventoryOpt.isEmpty()) {
                logger.warn("Product not found: {}", productId);
                return false;
            }

            Inventory inventory = inventoryOpt.get();
            
            if (inventory.getAvailableQuantity() < quantity) {
                logger.warn("Insufficient stock for product: {}, available: {}, requested: {}", 
                           productId, inventory.getAvailableQuantity(), quantity);
                return false;
            }

            // Reserve the stock
            inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
            inventoryRepository.save(inventory);

            // Publish inventory event
            publishInventoryEvent("STOCK_RESERVED", inventory, orderId);

            logger.info("Stock reserved successfully for order: {}", orderId);
            return true;

        } catch (OptimisticLockingFailureException e) {
            logger.warn("Optimistic locking failure for product: {}, retrying...", productId);
            throw e; // Will trigger retry
        } catch (Exception e) {
            logger.error("Error reserving stock for order: {}", orderId, e);
            return false;
        }
    }

    /**
     * Release reserved stock.
     */
    @Retryable(value = {OptimisticLockingFailureException.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    @CacheEvict(value = {"inventory", "stock-availability", "products"}, allEntries = true)
    public boolean releaseReservedStock(UUID productId, Integer quantity, UUID orderId) {
        logger.info("Releasing reserved stock for order: {}, product: {}, quantity: {}", orderId, productId, quantity);

        try {
            Optional<Inventory> inventoryOpt = inventoryRepository.findByProductIdWithProduct(productId);
            
            if (inventoryOpt.isEmpty()) {
                logger.warn("Product not found: {}", productId);
                return false;
            }

            Inventory inventory = inventoryOpt.get();
            
            if (inventory.getReservedQuantity() < quantity) {
                logger.warn("Cannot release more than reserved for product: {}, reserved: {}, requested: {}", 
                           productId, inventory.getReservedQuantity(), quantity);
                return false;
            }

            // Release the reserved stock
            inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
            inventoryRepository.save(inventory);

            // Publish inventory event
            publishInventoryEvent("STOCK_RELEASED", inventory, orderId);

            logger.info("Reserved stock released successfully for order: {}", orderId);
            return true;

        } catch (OptimisticLockingFailureException e) {
            logger.warn("Optimistic locking failure for product: {}, retrying...", productId);
            throw e; // Will trigger retry
        } catch (Exception e) {
            logger.error("Error releasing reserved stock for order: {}", orderId, e);
            return false;
        }
    }

    /**
     * Confirm stock allocation (convert reserved to sold).
     */
    @Retryable(value = {OptimisticLockingFailureException.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    @CacheEvict(value = {"inventory", "stock-availability", "products"}, allEntries = true)
    public boolean confirmAllocation(UUID productId, Integer quantity, UUID orderId) {
        logger.info("Confirming allocation for order: {}, product: {}, quantity: {}", orderId, productId, quantity);

        try {
            Optional<Inventory> inventoryOpt = inventoryRepository.findByProductIdWithProduct(productId);
            
            if (inventoryOpt.isEmpty()) {
                logger.warn("Product not found: {}", productId);
                return false;
            }

            Inventory inventory = inventoryOpt.get();
            
            if (inventory.getReservedQuantity() < quantity) {
                logger.warn("Cannot confirm more than reserved for product: {}, reserved: {}, requested: {}", 
                           productId, inventory.getReservedQuantity(), quantity);
                return false;
            }

            // Confirm allocation: reduce both total and reserved quantities
            inventory.setQuantity(inventory.getQuantity() - quantity);
            inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
            inventoryRepository.save(inventory);

            // Publish inventory event
            publishInventoryEvent("STOCK_ALLOCATED", inventory, orderId);

            logger.info("Stock allocation confirmed for order: {}", orderId);
            return true;

        } catch (OptimisticLockingFailureException e) {
            logger.warn("Optimistic locking failure for product: {}, retrying...", productId);
            throw e; // Will trigger retry
        } catch (Exception e) {
            logger.error("Error confirming allocation for order: {}", orderId, e);
            return false;
        }
    }

    /**
     * Update inventory quantity.
     */
    @CacheEvict(value = {"inventory", "stock-availability", "products"}, allEntries = true)
    public boolean updateInventory(UUID productId, Integer newQuantity) {
        logger.info("Updating inventory for product: {}, new quantity: {}", productId, newQuantity);

        try {
            Optional<Inventory> inventoryOpt = inventoryRepository.findByProductIdWithProduct(productId);
            
            if (inventoryOpt.isEmpty()) {
                logger.warn("Product not found: {}", productId);
                return false;
            }

            Inventory inventory = inventoryOpt.get();
            Integer oldQuantity = inventory.getQuantity();
            inventory.setQuantity(newQuantity);
            inventoryRepository.save(inventory);

            // Publish inventory event
            publishInventoryEvent("INVENTORY_UPDATED", inventory, null);

            logger.info("Inventory updated for product: {}, old: {}, new: {}", 
                       productId, oldQuantity, newQuantity);
            return true;

        } catch (Exception e) {
            logger.error("Error updating inventory for product: {}", productId, e);
            return false;
        }
    }

    /**
     * Get low stock items.
     */
    @Transactional(readOnly = true)
    public List<InventoryResponse> getLowStockItems(Integer threshold) {
        logger.debug("Getting low stock items with threshold: {}", threshold);
        
        return inventoryRepository.findLowStockItems(threshold)
                .stream()
                .map(InventoryResponse::fromInventory)
                .collect(Collectors.toList());
    }

    /**
     * Get inventory by category.
     */
    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventoryByCategory(String category) {
        logger.debug("Getting inventory for category: {}", category);
        
        return inventoryRepository.findByProductCategory(category)
                .stream()
                .map(InventoryResponse::fromInventory)
                .collect(Collectors.toList());
    }

    /**
     * Get total inventory value.
     */
    @Cacheable(value = "inventory-value")
    @Transactional(readOnly = true)
    public Double getTotalInventoryValue() {
        logger.debug("Calculating total inventory value");
        
        Double value = inventoryRepository.getTotalInventoryValue();
        return value != null ? value : 0.0;
    }

    /**
     * Map inventory entity to response DTO.
     */
    private InventoryResponse mapToInventoryResponse(Inventory inventory) {
        return InventoryResponse.fromInventory(inventory);
    }

    /**
     * Publish inventory event to Kafka.
     */
    private void publishInventoryEvent(String eventType, Inventory inventory, UUID orderId) {
        try {
            InventoryEvent event = new InventoryEvent(
                eventType,
                inventory.getProductId(),
                inventory.getProduct().getSku(),
                inventory.getQuantity(),
                inventory.getReservedQuantity(),
                inventory.getAvailableQuantity(),
                orderId
            );

            kafkaTemplate.send(INVENTORY_TOPIC, inventory.getProductId().toString(), event);
            logger.debug("Published inventory event: {} for product: {}", eventType, inventory.getProductId());

        } catch (Exception e) {
            logger.error("Error publishing inventory event: {} for product: {}", 
                        eventType, inventory.getProductId(), e);
        }
    }
}
