package com.greenwich.flowerplus.dto.response;

import com.greenwich.flowerplus.common.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO for inventory transaction history.
 * Each transaction is an immutable snapshot of stock state at that moment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransactionResponse {

    private Long id;
    private Long materialId;
    private String materialName;

    /**
     * Type of transaction: IMPORT, SALE, USAGE, DAMAGED, ADJUST_UP, ADJUST_DOWN, RETURN, RESERVE, RELEASE
     */
    private TransactionType type;

    // ==================== QUANTITY CHANGES ====================

    /**
     * The quantity change (SIGNED).
     * - Positive: stock added (IMPORT, RETURN, ADJUST_UP)
     * - Negative: stock removed (SALE, USAGE, DAMAGED, ADJUST_DOWN)
     * - Zero: reservation changes only (RESERVE, RELEASE)
     */
    private Integer quantityDelta;

    /**
     * Quantity BEFORE this transaction was applied.
     */
    private Integer beforeQuantity;

    /**
     * Quantity AFTER this transaction was applied.
     * Should equal beforeQuantity + quantityDelta.
     */
    private Integer afterQuantity;

    /**
     * Stock balance AFTER this transaction (same as afterQuantity, for convenience).
     */
    private Integer currentBalance;

    // ==================== RESERVED QUANTITY CHANGES ====================

    /**
     * Reserved quantity BEFORE this transaction.
     */
    private Integer beforeReserved;

    /**
     * Reserved quantity AFTER this transaction.
     */
    private Integer afterReserved;

    // ==================== FINANCIAL SNAPSHOT ====================

    /**
     * Cost price snapshot at the time of transaction:
     * - For IMPORT: the new calculated MAC
     * - For deductions: the MAC at that moment
     */
    private BigDecimal costPrice;

    // ==================== TRACEABILITY ====================

    /**
     * Reference code for traceability (Order ID, Import Batch Code, etc.)
     */
    private String referenceCode;

    /**
     * Additional notes for this transaction
     */
    private String note;

    /**
     * When this transaction was created
     */
    private Instant createdAt;
}
