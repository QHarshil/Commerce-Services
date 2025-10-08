package com.commerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request DTO for stock reservation operations.
 */
public class StockReservationRequest {

    @JsonProperty("productId")
    @NotNull(message = "Product ID cannot be null")
    private UUID productId;

    @JsonProperty("quantity")
    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @JsonProperty("orderId")
    @NotNull(message = "Order ID cannot be null")
    private UUID orderId;

    // Constructors
    public StockReservationRequest() {}

    public StockReservationRequest(UUID productId, Integer quantity, UUID orderId) {
        this.productId = productId;
        this.quantity = quantity;
        this.orderId = orderId;
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

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return "StockReservationRequest{" +
                "productId=" + productId +
                ", quantity=" + quantity +
                ", orderId=" + orderId +
                '}';
    }
}
