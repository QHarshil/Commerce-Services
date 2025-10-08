package com.commerce.model;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Inventory entity with optimistic locking for concurrent stock management.
 */
@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @Column(name = "product_id")
    private UUID productId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    @NotNull(message = "Quantity cannot be null")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @Column(name = "reserved_quantity", nullable = false)
    @NotNull(message = "Reserved quantity cannot be null")
    @Min(value = 0, message = "Reserved quantity cannot be negative")
    private Integer reservedQuantity = 0;

    @Version
    @Column(nullable = false)
    private Integer version = 0;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Inventory() {}

    public Inventory(UUID productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
        this.reservedQuantity = 0;
    }

    public Inventory(Product product, Integer quantity) {
        this.product = product;
        this.productId = product.getId();
        this.quantity = quantity;
        this.reservedQuantity = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Get available quantity (total - reserved).
     */
    public Integer getAvailableQuantity() {
        return quantity - reservedQuantity;
    }

    /**
     * Check if sufficient stock is available.
     */
    public boolean hasAvailableStock(Integer requestedQuantity) {
        return getAvailableQuantity() >= requestedQuantity;
    }

    /**
     * Reserve stock for an order.
     */
    public void reserveStock(Integer quantityToReserve) {
        if (!hasAvailableStock(quantityToReserve)) {
            throw new IllegalStateException("Insufficient stock available");
        }
        this.reservedQuantity += quantityToReserve;
    }

    /**
     * Release reserved stock.
     */
    public void releaseReservedStock(Integer quantityToRelease) {
        if (quantityToRelease > this.reservedQuantity) {
            throw new IllegalStateException("Cannot release more than reserved");
        }
        this.reservedQuantity -= quantityToRelease;
    }

    /**
     * Confirm stock allocation (reduce actual quantity).
     */
    public void confirmAllocation(Integer quantityToConfirm) {
        if (quantityToConfirm > this.reservedQuantity) {
            throw new IllegalStateException("Cannot confirm more than reserved");
        }
        this.quantity -= quantityToConfirm;
        this.reservedQuantity -= quantityToConfirm;
    }

    // Getters and Setters
    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
        this.productId = product != null ? product.getId() : null;
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "productId=" + productId +
                ", quantity=" + quantity +
                ", reservedQuantity=" + reservedQuantity +
                ", availableQuantity=" + getAvailableQuantity() +
                ", version=" + version +
                '}';
    }
}
