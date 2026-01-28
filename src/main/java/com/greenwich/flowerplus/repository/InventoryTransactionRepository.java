package com.greenwich.flowerplus.repository;

import com.greenwich.flowerplus.common.enums.TransactionType;
import com.greenwich.flowerplus.entity.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    /**
     * Get paginated transaction history for a material.
     * Ordered by creation time descending (newest first).
     */
    @Query("SELECT t FROM InventoryTransaction t WHERE t.material.id = :materialId ORDER BY t.createdAt DESC")
    Page<InventoryTransaction> findByMaterialId(@Param("materialId") Long materialId, Pageable pageable);

    /**
     * Find transactions by reference code (e.g., Order ID, Import Batch Code).
     */
    List<InventoryTransaction> findByReferenceCode(String referenceCode);

    /**
     * Find transactions by material ID and type.
     */
    @Query("SELECT t FROM InventoryTransaction t WHERE t.material.id = :materialId AND t.type = :type ORDER BY t.createdAt DESC")
    Page<InventoryTransaction> findByMaterialIdAndType(
            @Param("materialId") Long materialId,
            @Param("type") TransactionType type,
            Pageable pageable
    );
}
