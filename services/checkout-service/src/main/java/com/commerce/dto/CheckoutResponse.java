package com.commerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Checkout response DTO.
 */
public class CheckoutResponse {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("orderId")
    private UUID orderId;

    @JsonProperty("checkoutId")
    private UUID checkoutId;

    @JsonProperty("totalAmount")
    private BigDecimal totalAmount;

    @JsonProperty("currency")
    private String currency = "USD";

    @JsonProperty("status")
    private String status;

    @JsonProperty("errorMessage")
    private String errorMessage;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("processingTimeMs")
    private Long processingTimeMs;

    // Constructors
    public CheckoutResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public CheckoutResponse(boolean success, UUID orderId, UUID checkoutId, 
                           BigDecimal totalAmount, String status) {
        this.success = success;
        this.orderId = orderId;
        this.checkoutId = checkoutId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    // Static factory methods
    public static CheckoutResponse success(UUID orderId, UUID checkoutId, 
                                         BigDecimal totalAmount, String status) {
        return new CheckoutResponse(true, orderId, checkoutId, totalAmount, status);
    }

    public static CheckoutResponse failure(String errorMessage) {
        CheckoutResponse response = new CheckoutResponse();
        response.success = false;
        response.errorMessage = errorMessage;
        response.status = "FAILED";
        return response;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getCheckoutId() {
        return checkoutId;
    }

    public void setCheckoutId(UUID checkoutId) {
        this.checkoutId = checkoutId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
}
