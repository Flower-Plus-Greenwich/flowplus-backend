package com.greenwich.flowerplus.service;

import com.greenwich.flowerplus.common.enums.TransactionType;
import com.greenwich.flowerplus.dto.response.InventoryTransactionResponse;
import com.greenwich.flowerplus.entity.Material;
import com.greenwich.flowerplus.entity.MaterialStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

/**
 * Service dedicated to transaction ledger management.
 * Handles all inventory transaction logging with proper snapshot pattern.
 * 
 * <p>Separation of Concerns:</p>
 * <ul>
 *   <li>MaterialStockService: Stock logic (Calculate MAC, Check Availability, Update DB)</li>
 *   <li>InventoryTransactionService: Ledger logic (Create and save audit records)</li>
 * </ul>
 * 
 * <p>This service should NOT depend on MaterialStockService (no cyclic dependencies).</p>
 */
public interface InventoryTransactionService {

    // ==================== IMPORT OPERATIONS ====================

    /**
     * Log an import transaction (positive delta).
     * Called AFTER stock update with pessimistic lock.
     *
     * @param material      The material entity
     * @param beforeQty     Quantity before import
     * @param afterQty      Quantity after import
     * @param reservedQty   Current reserved quantity (unchanged by import)
     * @param delta         Import quantity (positive)
     * @param newCostPrice  New MAC after calculation
     * @param referenceCode Import batch/invoice code
     * @param note          Optional note
     */
    void logImportTransaction(Material material, int beforeQty, int afterQty,
                              int reservedQty, int delta, BigDecimal newCostPrice,
                              String referenceCode, String note);

    // ==================== DEDUCTION OPERATIONS ====================

    /**
     * Log a deduction transaction (negative delta).
     * For SALE, USAGE, DAMAGED operations.
     *
     * @param snapshot      MaterialStock snapshot BEFORE atomic update
     * @param delta         Deduction quantity (positive, will be negated internally)
     * @param type          Transaction type (SALE, USAGE, DAMAGED)
     * @param referenceCode Order ID or other reference
     * @param note          Optional note
     */
    void logDeductTransaction(MaterialStock snapshot, int delta,
                              TransactionType type, String referenceCode, String note);

    // ==================== ADJUSTMENT OPERATIONS ====================

    /**
     * Log a manual adjustment transaction.
     *
     * @param snapshot      Current stock state BEFORE adjustment
     * @param delta         Adjustment amount (positive for ADJUST_UP, negative for ADJUST_DOWN)
     * @param type          ADJUST_UP or ADJUST_DOWN
     * @param note          Required note explaining adjustment reason
     */
    void logAdjustmentTransaction(MaterialStock snapshot, int delta,
                                   TransactionType type, String note);

    // ==================== RESERVATION OPERATIONS ====================

    /**
     * Log reservation change (RESERVE).
     *
     * @param snapshot       Stock BEFORE reservation change
     * @param reserveDelta   Amount being reserved (positive)
     * @param referenceCode  Order ID
     */
    void logReserveTransaction(MaterialStock snapshot, int reserveDelta, String referenceCode);

    /**
     * Log release of reservation (RELEASE).
     *
     * @param snapshot       Stock BEFORE release
     * @param releaseDelta   Amount being released (positive, will be negated for reserved)
     * @param referenceCode  Order ID
     * @param note           Reason for release (e.g., "Order cancelled")
     */
    void logReleaseTransaction(MaterialStock snapshot, int releaseDelta,
                               String referenceCode, String note);

    /**
     * Log return transaction (stock coming back).
     *
     * @param snapshot       Current stock state
     * @param returnQty      Quantity returned (positive)
     * @param referenceCode  Original order ID
     * @param note           Reason for return
     */
    void logReturnTransaction(MaterialStock snapshot, int returnQty,
                              String referenceCode, String note);

    // ==================== QUERY OPERATIONS ====================

    /**
     * Get paginated transaction history for a material.
     *
     * @param materialId Material ID
     * @param pageable   Pagination parameters
     * @return Page of transaction records, ordered by creation time descending
     */
    Page<InventoryTransactionResponse> getTransactionHistory(Long materialId, Pageable pageable);
}
