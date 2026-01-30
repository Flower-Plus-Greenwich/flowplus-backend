package com.greenwich.flowerplus.service.impl;

import com.greenwich.flowerplus.common.enums.ErrorCode;
import com.greenwich.flowerplus.common.enums.TransactionType;
import com.greenwich.flowerplus.common.exception.AppException;
import com.greenwich.flowerplus.dto.request.MaterialStockDeductRequest;
import com.greenwich.flowerplus.dto.request.MaterialStockImportRequest;
import com.greenwich.flowerplus.dto.response.InventoryTransactionResponse;
import com.greenwich.flowerplus.dto.response.MaterialStockResponse;
import com.greenwich.flowerplus.entity.Material;
import com.greenwich.flowerplus.entity.MaterialStock;
import com.greenwich.flowerplus.repository.MaterialRepository;
import com.greenwich.flowerplus.repository.MaterialStockRepository;
import com.greenwich.flowerplus.service.InventoryTransactionService;
import com.greenwich.flowerplus.service.MaterialStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Implementation of MaterialStockService with:
 * - Moving Average Cost (MAC) calculation on imports
 * - Hybrid Locking Strategy:
 *   - PESSIMISTIC LOCK for Admin operations (Import, Manual Adjust)
 *   - ATOMIC UPDATE for Sales operations (Deduct for Order)
 * - Transaction logging delegated to InventoryTransactionService (SRP)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MaterialStockServiceImpl implements MaterialStockService {

    private final MaterialStockRepository stockRepository;
    private final MaterialRepository materialRepository;
    private final InventoryTransactionService transactionService;

    // ==================== ADMIN OPERATIONS (Pessimistic Lock) ====================

    /**
     * Import materials into stock with MAC calculation.
     * Uses PESSIMISTIC_WRITE lock to prevent lost updates on cost price.
     *
     * MAC Formula: NewCost = ((CurrentQty × CurrentCost) + (ImportQty × ImportPrice)) / (CurrentQty + ImportQty)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaterialStockResponse importStock(MaterialStockImportRequest request) {
        log.info("Importing stock: materialId={}, qty={}, price={}",
                request.getMaterialId(), request.getQuantity(), request.getImportPrice());

        // 1. LOCK: Fetch stock with pessimistic write lock
        MaterialStock stock = stockRepository.findByMaterialIdForUpdate(request.getMaterialId())
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));

        // 2. SNAPSHOT: Capture before state
        int beforeQty = stock.getQuantity();
        int reservedQty = stock.getReservedQuantity();

        // 3. CALCULATE: Apply MAC formula
        BigDecimal newCostPrice = calculateMac(
                beforeQty, stock.getCostPrice(),
                request.getQuantity(), request.getImportPrice()
        );

        // 4. UPDATE: Set new quantity and cost price
        int afterQty = beforeQty + request.getQuantity();
        stock.setQuantity(afterQty);
        stock.setCostPrice(newCostPrice);
        stockRepository.save(stock);

        log.info("Stock updated: qty={}, costPrice={}", afterQty, newCostPrice);

        // 5. LOG: Delegate to TransactionService
        transactionService.logImportTransaction(
                stock.getMaterial(),
                beforeQty, afterQty, reservedQty,
                request.getQuantity(),
                newCostPrice,
                request.getReferenceCode(),
                request.getNote()
        );

        return toResponse(stock);
    }

    /**
     * Deduct materials from stock (for admin/manual operations).
     * Uses PESSIMISTIC LOCK for integrity.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaterialStockResponse deductStock(MaterialStockDeductRequest request) {
        log.info("Deducting stock (admin): materialId={}, qty={}, type={}",
                request.getMaterialId(), request.getQuantity(), request.getType());

        // Validate: Only deduction types allowed
        validateDeductionType(request.getType());

        // 1. LOCK: Pessimistic lock
        MaterialStock stock = stockRepository.findByMaterialIdForUpdate(request.getMaterialId())
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));

        // 2. CHECK: Availability
        int availableStock = stock.getAvailableStock();
        if (availableStock < request.getQuantity()) {
            log.warn("Insufficient stock: available={}, requested={}", availableStock, request.getQuantity());
            throw new AppException(ErrorCode.INVENTORY_INSUFFICIENT_STOCK);
        }

        // 3. SNAPSHOT: Before update
        MaterialStock snapshot = createSnapshot(stock);

        // 4. UPDATE: Decrease quantity
        stock.setQuantity(stock.getQuantity() - request.getQuantity());
        stockRepository.save(stock);

        // 5. LOG: Delegate to TransactionService
        transactionService.logDeductTransaction(
                snapshot,
                request.getQuantity(),
                request.getType(),
                request.getReferenceCode(),
                request.getNote()
        );

        return toResponse(stock);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaterialStockResponse adjustStock(Long materialId, int delta, String note) {
        log.info("Adjusting stock: materialId={}, delta={}", materialId, delta);

        // 1. LOCK: Pessimistic lock
        MaterialStock stock = stockRepository.findByMaterialIdForUpdate(materialId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));

        // 2. VALIDATE: Cannot go negative
        int newQty = stock.getQuantity() + delta;
        if (newQty < 0) {
            throw new AppException(ErrorCode.INVENTORY_INVALID_QUANTITY);
        }

        // 3. SNAPSHOT
        MaterialStock snapshot = createSnapshot(stock);

        // 4. UPDATE
        stock.setQuantity(newQty);
        stockRepository.save(stock);

        // 5. LOG
        TransactionType type = delta >= 0 ? TransactionType.ADJUST_UP : TransactionType.ADJUST_DOWN;
        transactionService.logAdjustmentTransaction(snapshot, delta, type, note);

        return toResponse(stock);
    }

    // ==================== SALES OPERATIONS (Atomic Update) ====================

    /**
     * Deduct stock for a sales order using ATOMIC UPDATE.
     * High-concurrency optimized - no database lock required.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductForOrder(Long materialId, int quantity, String orderCode) {
        log.info("Deducting for order: materialId={}, qty={}, order={}",
                materialId, quantity, orderCode);

        // 1. SNAPSHOT: Read current state (no lock) - for logging purposes
        MaterialStock snapshot = stockRepository.findByMaterialId(materialId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));

        // 2. ATOMIC UPDATE: The real action
        int rowsModified = stockRepository.atomicDeductStock(materialId, quantity);

        // 3. CHECK: If 0 rows, insufficient stock
        if (rowsModified == 0) {
            log.warn("Atomic deduct failed: materialId={}, requested={}, available={}",
                    materialId, quantity, snapshot.getQuantity());
            throw new AppException(ErrorCode.INVENTORY_INSUFFICIENT_STOCK);
        }

        // 4. LOG: Delegate to TransactionService
        // Using snapshot for "before" state - self-consistent audit record
        transactionService.logDeductTransaction(
                snapshot,
                quantity,
                TransactionType.SALE,
                orderCode,
                "Deducted for order: " + orderCode
        );

        log.info("Stock deducted atomically: materialId={}, qty={}", materialId, quantity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reserveForOrder(Long materialId, int quantity, String orderCode) {
        log.info("Reserving for order: materialId={}, qty={}, order={}",
                materialId, quantity, orderCode);

        // 1. SNAPSHOT
        MaterialStock snapshot = stockRepository.findByMaterialId(materialId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));

        // 2. ATOMIC RESERVE
        int rowsModified = stockRepository.atomicReserveStock(materialId, quantity);

        // 3. CHECK
        if (rowsModified == 0) {
            log.warn("Reserve failed: materialId={}, requested={}, available={}",
                    materialId, quantity, snapshot.getAvailableStock());
            throw new AppException(ErrorCode.INVENTORY_RESERVE_FAILED);
        }

        // 4. LOG
        transactionService.logReserveTransaction(snapshot, quantity, orderCode);

        log.info("Stock reserved: materialId={}, qty={}", materialId, quantity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseReservation(Long materialId, int quantity, String orderCode, String reason) {
        log.info("Releasing reservation: materialId={}, qty={}, order={}",
                materialId, quantity, orderCode);

        // 1. SNAPSHOT
        MaterialStock snapshot = stockRepository.findByMaterialId(materialId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));

        // 2. ATOMIC RELEASE
        int rowsModified = stockRepository.atomicReleaseStock(materialId, quantity);

        // 3. CHECK
        if (rowsModified == 0) {
            log.warn("Release failed: materialId={}, requested={}, reserved={}",
                    materialId, quantity, snapshot.getReservedQuantity());
            throw new AppException(ErrorCode.INVENTORY_RELEASE_FAILED);
        }

        // 4. LOG
        transactionService.logReleaseTransaction(snapshot, quantity, orderCode, reason);

        log.info("Reservation released: materialId={}, qty={}", materialId, quantity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReservation(Long materialId, int quantity, String orderCode) {
        log.info("Confirming reservation: materialId={}, qty={}, order={}",
                materialId, quantity, orderCode);

        // 1. SNAPSHOT
        MaterialStock snapshot = stockRepository.findByMaterialId(materialId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));

        // 2. ATOMIC CONFIRM (decreases both quantity and reservedQuantity)
        int rowsModified = stockRepository.atomicConfirmReservation(materialId, quantity);

        // 3. CHECK
        if (rowsModified == 0) {
            log.warn("Confirm failed: materialId={}, requested={}, reserved={}",
                    materialId, quantity, snapshot.getReservedQuantity());
            throw new AppException(ErrorCode.INVENTORY_CONFIRM_FAILED);
        }

        // 4. LOG - This is a SALE transaction
        transactionService.logDeductTransaction(
                snapshot, quantity, TransactionType.SALE,
                orderCode, "Confirmed reservation for order: " + orderCode
        );

        log.info("Reservation confirmed: materialId={}, qty={}", materialId, quantity);
    }

    // ==================== QUERY OPERATIONS ====================

    @Override
    @Transactional(readOnly = true)
    public MaterialStockResponse getStockByMaterialId(Long materialId) {
        MaterialStock stock = stockRepository.findByMaterialId(materialId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));
        return toResponse(stock);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryTransactionResponse> getTransactionHistory(Long materialId, Pageable pageable) {
        // Delegate to TransactionService
        return transactionService.getTransactionHistory(materialId, pageable);
    }

    // ==================== SETUP OPERATIONS ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaterialStockResponse initializeStock(Long materialId) {
        log.info("Initializing stock for material {}", materialId);

        // Check if stock already exists
        if (stockRepository.findByMaterialId(materialId).isPresent()) {
            throw new AppException(ErrorCode.RESOURCE_CONFLICT);
        }

        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new AppException(ErrorCode.MATERIAL_NOT_FOUND));

        MaterialStock stock = MaterialStock.builder()
                .material(material)
                .quantity(0)
                .reservedQuantity(0)
                .costPrice(BigDecimal.ZERO)
                .reorderLevel(10)
                .build();
        stock = stockRepository.save(stock);

        log.info("Stock initialized for material {}", materialId);

        return toResponse(stock);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaterialStockResponse updateReorderLevel(Long materialId, Integer reorderLevel, Long version) {
        log.info("Updating reorder level: materialId={}, level={}, version={}",
                materialId, reorderLevel, version);

        MaterialStock stock = stockRepository.findByMaterialId(materialId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));

        // Version check - JPA will throw OptimisticLockException if versions don't match
        if (!stock.getVersion().equals(version)) {
            log.warn("Version mismatch: expected={}, actual={}", version, stock.getVersion());
            throw new AppException(ErrorCode.RESOURCE_CONFLICT);
        }

        stock.setReorderLevel(reorderLevel);
        stock = stockRepository.save(stock);

        log.info("Reorder level updated for material {}", materialId);

        return toResponse(stock);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Calculate Moving Average Cost (MAC).
     * Formula: ((CurrentQty × CurrentCost) + (ImportQty × ImportPrice)) / (CurrentQty + ImportQty)
     */
    private BigDecimal calculateMac(int currentQty, BigDecimal currentCost,
                                    int importQty, BigDecimal importPrice) {
        if (currentQty == 0) {
            return importPrice.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal currentTotalValue = currentCost.multiply(BigDecimal.valueOf(currentQty));
        BigDecimal importTotalValue = importPrice.multiply(BigDecimal.valueOf(importQty));
        BigDecimal newTotalQty = BigDecimal.valueOf(currentQty + importQty);

        return currentTotalValue.add(importTotalValue)
                .divide(newTotalQty, 2, RoundingMode.HALF_UP);
    }

    /**
     * Create a snapshot of current stock state for logging.
     */
    private MaterialStock createSnapshot(MaterialStock stock) {
        return MaterialStock.builder()
                .material(stock.getMaterial())
                .quantity(stock.getQuantity())
                .reservedQuantity(stock.getReservedQuantity())
                .costPrice(stock.getCostPrice())
                .reorderLevel(stock.getReorderLevel())
                .build();
    }

    /**
     * Validate that the transaction type is valid for deduction.
     */
    private void validateDeductionType(TransactionType type) {
        if (type == TransactionType.IMPORT || 
            type == TransactionType.RETURN ||
            type == TransactionType.ADJUST_UP ||
            type == TransactionType.RESERVE ||
            type == TransactionType.RELEASE) {
            throw new AppException(ErrorCode.INVALID_TRANSACTION_TYPE);
        }
    }

    /**
     * Convert MaterialStock entity to response DTO.
     */
    private MaterialStockResponse toResponse(MaterialStock stock) {
        Material material = stock.getMaterial();
        int availableStock = stock.getAvailableStock();

        return MaterialStockResponse.builder()
                .materialId(material.getId())
                .materialName(material.getName())
                .unit(material.getUnit())
                .quantity(stock.getQuantity())
                .reservedQuantity(stock.getReservedQuantity())
                .availableStock(availableStock)
                .costPrice(stock.getCostPrice())
                .totalValue(stock.getTotalValue())
                .reorderLevel(stock.getReorderLevel())
                .lowStock(availableStock <= stock.getReorderLevel())
                .version(stock.getVersion())
                .build();
    }
}
