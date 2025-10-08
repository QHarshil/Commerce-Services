package com.commerce.repository;

import com.commerce.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for inventory operations with optimistic locking support.
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    /**
     * Find inventory with product details.
     */
    @Query("SELECT i FROM Inventory i JOIN FETCH i.product WHERE i.productId = :productId")
    Optional<Inventory> findByProductIdWithProduct(@Param("productId") UUID productId);

    /**
     * Find inventory by product SKU.
     */
    @Query("SELECT i FROM Inventory i JOIN FETCH i.product p WHERE p.sku = :sku")
    Optional<Inventory> findByProductSku(@Param("sku") String sku);

    /**
     * Find all inventory items with low stock.
     */
    @Query("SELECT i FROM Inventory i JOIN FETCH i.product WHERE (i.quantity - i.reservedQuantity) <= :threshold")
    List<Inventory> findLowStockItems(@Param("threshold") Integer threshold);

    /**
     * Find inventory items by category.
     */
    @Query("SELECT i FROM Inventory i JOIN FETCH i.product p WHERE p.category = :category")
    List<Inventory> findByProductCategory(@Param("category") String category);

    /**
     * Check stock availability for multiple products.
     */
    @Query("SELECT i.productId, (i.quantity - i.reservedQuantity) as available " +
           "FROM Inventory i WHERE i.productId IN :productIds")
    List<Object[]> checkStockAvailability(@Param("productIds") List<UUID> productIds);

    /**
     * Get total inventory value.
     */
    @Query("SELECT SUM(i.quantity * p.price) FROM Inventory i JOIN i.product p")
    Double getTotalInventoryValue();

    /**
     * Find inventory with pessimistic lock for critical updates.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId")
    Optional<Inventory> findByProductIdWithLock(@Param("productId") UUID productId);

    /**
     * Bulk update reserved quantities.
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.reservedQuantity = i.reservedQuantity + :quantity " +
           "WHERE i.productId = :productId AND (i.quantity - i.reservedQuantity) >= :quantity")
    int reserveStock(@Param("productId") UUID productId, @Param("quantity") Integer quantity);

    /**
     * Bulk release reserved stock.
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.reservedQuantity = i.reservedQuantity - :quantity " +
           "WHERE i.productId = :productId AND i.reservedQuantity >= :quantity")
    int releaseReservedStock(@Param("productId") UUID productId, @Param("quantity") Integer quantity);
}
