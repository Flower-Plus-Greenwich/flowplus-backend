package com.greenwich.flowerplus.service;

import com.greenwich.flowerplus.dto.request.MaterialStockDeductRequest;
import com.greenwich.flowerplus.dto.request.MaterialStockImportRequest;
import com.greenwich.flowerplus.dto.response.InventoryTransactionResponse;
import com.greenwich.flowerplus.dto.response.MaterialStockResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service for managing material stock with:
 * - Moving Average Cost (MAC) calculation on imports
 * - Hybrid locking strategy (Pessimistic for admin, Atomic for sales)
 * - Transaction logging delegated to InventoryTransactionService
 */
public interface MaterialStockService {

    // ==================== ADMIN OPERATIONS (Pessimistic Lock) ====================

    /**
     * Import materials into stock.
     * Uses PESSIMISTIC LOCK for MAC calculation integrity.
     * 
     * This operation:
     * 1. Acquires a pessimistic write lock on the stock record
     * 2. Calculates new MAC using weighted average formula
     * 3. Updates stock quantity and cost price
     * 4. Delegates transaction logging to InventoryTransactionService
     *
     * @param request Import request with materialId, quantity, and importPrice
     * @return Updated stock information
     */
    MaterialStockResponse importStock(MaterialStockImportRequest request);

    /**
     * Deduct materials from stock (for manual admin operations).
     * Uses PESSIMISTIC LOCK for availability check integrity.
     * For usage, damage, or manual adjustments.
     * 
     * Note: For high-concurrency sales, use deductForOrder() instead.
     *
     * @param request Deduct request with materialId, quantity, type, and referenceCode
     * @return Updated stock information
     */
    MaterialStockResponse deductStock(MaterialStockDeductRequest request);

    /**
     * Manual stock adjustment (for inventory audits).
     * Uses PESSIMISTIC LOCK.
     *
     * @param materialId Material ID
     * @param delta      Adjustment amount (positive for ADJUST_UP, negative for ADJUST_DOWN)
     * @param note       Required note explaining adjustment reason
     * @return Updated stock information
     */
    MaterialStockResponse adjustStock(Long materialId, int delta, String note);

    // ==================== SALES OPERATIONS (Atomic Update) ====================

    /**
     * Deduct stock for a sales order using ATOMIC UPDATE.
     * Optimized for high concurrency - no database lock required.
     * 
     * Flow:
     * 1. Read snapshot (no lock) for logging purposes
     * 2. Execute atomic UPDATE with WHERE check
     * 3. If 0 rows modified, throw INVENTORY_INSUFFICIENT_STOCK
     * 4. Delegate transaction logging to InventoryTransactionService
     *
     * @param materialId Material ID
     * @param quantity   Quantity to deduct (positive value)
     * @param orderCode  Reference to the order for traceability
     * @throws com.greenwich.flowerplus.common.exception.AppException 
     *         INVENTORY_INSUFFICIENT_STOCK if atomic update fails
     */
    void deductForOrder(Long materialId, int quantity, String orderCode);

    /**
     * Reserve stock for a pending order using ATOMIC UPDATE.
     * Reserved stock is not available for other orders.
     *
     * @param materialId Material ID
     * @param quantity   Quantity to reserve
     * @param orderCode  Order reference
     * @throws com.greenwich.flowerplus.common.exception.AppException 
     *         INVENTORY_RESERVE_FAILED if insufficient available stock
     */
    void reserveForOrder(Long materialId, int quantity, String orderCode);

    /**
     * Release reserved stock (order cancelled) using ATOMIC UPDATE.
     *
     * @param materialId Material ID
     * @param quantity   Quantity to release
     * @param orderCode  Order reference
     * @param reason     Reason for release
     */
    void releaseReservation(Long materialId, int quantity, String orderCode, String reason);

    /**
     * Confirm reservation and deduct stock (order ships) using ATOMIC UPDATE.
     * Converts reserved quantity to actual deduction.
     *
     * @param materialId Material ID
     * @param quantity   Quantity to confirm (must be currently reserved)
     * @param orderCode  Order reference
     */
    void confirmReservation(Long materialId, int quantity, String orderCode);

    // ==================== QUERY OPERATIONS ====================

    /**
     * Get current stock information for a material.
     *
     * @param materialId Material ID
     * @return Stock information including quantity, cost price, and total value
     */
    MaterialStockResponse getStockByMaterialId(Long materialId);

    /**
     * Get paginated transaction history for a material.
     * Delegates to InventoryTransactionService.
     *
     * @param materialId Material ID
     * @param pageable Pagination parameters
     * @return Page of transaction records
     */
    Page<InventoryTransactionResponse> getTransactionHistory(Long materialId, Pageable pageable);

    // ==================== SETUP OPERATIONS ====================

    /**
     * Initialize stock record for a new material.
     * Called automatically when creating a new material.
     *
     * @param materialId Material ID
     * @return Created stock with quantity=0 and costPrice=0
     */
    MaterialStockResponse initializeStock(Long materialId);

    /**
     * Update reorder level for a material's stock.
     * Uses optimistic locking to prevent lost updates.
     *
     * @param materialId Material ID
     * @param reorderLevel New reorder level threshold
     * @param version Current version for optimistic locking
     * @return Updated stock information
     */
    MaterialStockResponse updateReorderLevel(Long materialId, Integer reorderLevel, Long version);
}
