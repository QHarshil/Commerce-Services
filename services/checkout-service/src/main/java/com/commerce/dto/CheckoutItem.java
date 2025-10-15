package com.commerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Checkout item DTO.
 */
public class CheckoutItem {

    @JsonProperty("productId")
    @NotNull(message = "Product ID cannot be null")
    private UUID productId;

    @JsonProperty("quantity")
    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    // Constructors
    public CheckoutItem() {}

    public CheckoutItem(UUID productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    // Getters and Setters
    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "CheckoutItem{" +
                "productId=" + productId +
                ", quantity=" + quantity +
                '}';
    }
}
