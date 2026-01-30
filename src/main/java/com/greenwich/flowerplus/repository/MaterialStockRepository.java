package com.greenwich.flowerplus.repository;

import com.greenwich.flowerplus.entity.MaterialStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MaterialStockRepository extends JpaRepository<MaterialStock, Long> {

    /**
     * Find MaterialStock by material ID.
     * For read operations where locking is not required.
     */
    @Query("SELECT s FROM MaterialStock s WHERE s.material.id = :materialId")
    Optional<MaterialStock> findByMaterialId(@Param("materialId") Long materialId);

    /**
     * Find MaterialStock with PESSIMISTIC_WRITE lock.
     * Use this for Import operations to ensure MAC calculation integrity.
     * This prevents "Lost Update" during concurrent price calculations.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM MaterialStock s WHERE s.material.id = :materialId")
    Optional<MaterialStock> findByMaterialIdForUpdate(@Param("materialId") Long materialId);

    // ==================== ATOMIC OPERATIONS (High Concurrency) ====================

    /**
     * Atomic deduction for sales/orders.
     * Returns number of rows modified (0 = insufficient stock).
     * No lock required - uses atomic UPDATE with WHERE check.
     *
     * @param materialId Material ID
     * @param amount     Quantity to deduct (positive value)
     * @return 1 if successful, 0 if insufficient stock
     */
    @Modifying
    @Query("UPDATE MaterialStock s SET s.quantity = s.quantity - :amount " +
            "WHERE s.material.id = :materialId AND s.quantity >= :amount")
    int atomicDeductStock(@Param("materialId") Long materialId, @Param("amount") int amount);

    /**
     * Atomic reservation for order placement.
     * Checks available stock (quantity - reservedQuantity) before reserving.
     *
     * @param materialId Material ID
     * @param amount     Quantity to reserve (positive value)
     * @return 1 if successful, 0 if insufficient available stock
     */
    @Modifying
    @Query("UPDATE MaterialStock s SET s.reservedQuantity = s.reservedQuantity + :amount " +
            "WHERE s.material.id = :materialId AND (s.quantity - s.reservedQuantity) >= :amount")
    int atomicReserveStock(@Param("materialId") Long materialId, @Param("amount") int amount);

    /**
     * Atomic release of reserved stock (order cancelled).
     *
     * @param materialId Material ID
     * @param amount     Quantity to release (positive value)
     * @return 1 if successful, 0 if insufficient reserved
     */
    @Modifying
    @Query("UPDATE MaterialStock s SET s.reservedQuantity = s.reservedQuantity - :amount " +
            "WHERE s.material.id = :materialId AND s.reservedQuantity >= :amount")
    int atomicReleaseStock(@Param("materialId") Long materialId, @Param("amount") int amount);

    /**
     * Atomic confirm reservation (convert reserved to deducted when order ships).
     * Decreases both quantity and reservedQuantity.
     *
     * @param materialId Material ID
     * @param amount     Quantity to confirm (must be currently reserved)
     * @return 1 if successful, 0 if insufficient reserved
     */
    @Modifying
    @Query("UPDATE MaterialStock s SET s.quantity = s.quantity - :amount, " +
            "s.reservedQuantity = s.reservedQuantity - :amount " +
            "WHERE s.material.id = :materialId AND s.reservedQuantity >= :amount")
    int atomicConfirmReservation(@Param("materialId") Long materialId, @Param("amount") int amount);
}
