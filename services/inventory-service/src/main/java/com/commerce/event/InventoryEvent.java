package com.commerce.event;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Inventory event for Kafka messaging.
 */
@ConditionalOnProperty(name = "inventory.events.enabled", havingValue = "true", matchIfMissing = true)
@Component
public class InventoryEvent {

    @JsonProperty("eventType")
    private String eventType;

    @JsonProperty("productId")
    private UUID productId;

    @JsonProperty("sku")
    private String sku;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("reservedQuantity")
    private Integer reservedQuantity;

    @JsonProperty("availableQuantity")
    private Integer availableQuantity;

    @JsonProperty("orderId")
    private UUID orderId;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    // Constructors
    public InventoryEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public InventoryEvent(String eventType, UUID productId, String sku, 
                         Integer quantity, Integer reservedQuantity, 
                         Integer availableQuantity, UUID orderId) {
        this.eventType = eventType;
        this.productId = productId;
        this.sku = sku;
        this.quantity = quantity;
        this.reservedQuantity = reservedQuantity;
        this.availableQuantity = availableQuantity;
        this.orderId = orderId;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "InventoryEvent{" +
                "eventType='" + eventType + '\'' +
                ", productId=" + productId +
                ", sku='" + sku + '\'' +
                ", availableQuantity=" + availableQuantity +
                ", orderId=" + orderId +
                ", timestamp=" + timestamp +
                '}';
    }
}
