package com.commerce.dto;

import com.commerce.model.Inventory;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for inventory operations.
 */
public class InventoryResponse {

    @JsonProperty("productId")
    private UUID productId;

    @JsonProperty("sku")
    private String sku;

    @JsonProperty("productName")
    private String productName;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("reservedQuantity")
    private Integer reservedQuantity;

    @JsonProperty("availableQuantity")
    private Integer availableQuantity;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    // Constructors
    public InventoryResponse() {}

    public InventoryResponse(UUID productId, String sku, String productName, 
                           Integer quantity, Integer reservedQuantity, 
                           Integer availableQuantity, LocalDateTime updatedAt) {
        this.productId = productId;
        this.sku = sku;
        this.productName = productName;
        this.quantity = quantity;
        this.reservedQuantity = reservedQuantity;
        this.availableQuantity = availableQuantity;
        this.updatedAt = updatedAt;
    }

    /**
     * Create response from Inventory entity.
     */
    public static InventoryResponse fromInventory(Inventory inventory) {
        return new InventoryResponse(
            inventory.getProductId(),
            inventory.getProduct() != null ? inventory.getProduct().getSku() : null,
            inventory.getProduct() != null ? inventory.getProduct().getName() : null,
            inventory.getQuantity(),
            inventory.getReservedQuantity(),
            inventory.getAvailableQuantity(),
            inventory.getUpdatedAt()
        );
    }

    // Getters and Setters
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
