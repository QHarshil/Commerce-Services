package com.commerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/**
 * Checkout request DTO.
 */
public class CheckoutRequest {

    @JsonProperty("customerId")
    @NotNull(message = "Customer ID cannot be null")
    private UUID customerId;

    @JsonProperty("items")
    @NotEmpty(message = "Items cannot be empty")
    @Valid
    private List<CheckoutItem> items;

    @JsonProperty("paymentMethod")
    @NotNull(message = "Payment method cannot be null")
    private String paymentMethod;

    @JsonProperty("idempotencyKey")
    private String idempotencyKey;

    // Constructors
    public CheckoutRequest() {}

    public CheckoutRequest(UUID customerId, List<CheckoutItem> items, String paymentMethod) {
        this.customerId = customerId;
        this.items = items;
        this.paymentMethod = paymentMethod;
    }

    // Getters and Setters
    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public List<CheckoutItem> getItems() {
        return items;
    }

    public void setItems(List<CheckoutItem> items) {
        this.items = items;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    @Override
    public String toString() {
        return "CheckoutRequest{" +
                "customerId=" + customerId +
                ", items=" + items.size() +
                ", paymentMethod='" + paymentMethod + '\'' +
                '}';
    }
}
