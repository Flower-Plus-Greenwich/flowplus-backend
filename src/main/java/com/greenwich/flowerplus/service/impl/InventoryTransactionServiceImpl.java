package com.greenwich.flowerplus.service.impl;

import com.greenwich.flowerplus.common.enums.TransactionType;
import com.greenwich.flowerplus.dto.response.InventoryTransactionResponse;
import com.greenwich.flowerplus.entity.InventoryTransaction;
import com.greenwich.flowerplus.entity.Material;
import com.greenwich.flowerplus.entity.MaterialStock;
import com.greenwich.flowerplus.repository.InventoryTransactionRepository;
import com.greenwich.flowerplus.service.InventoryTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Implementation of InventoryTransactionService.
 * Handles all transaction ledger operations with proper before/after snapshots.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryTransactionServiceImpl implements InventoryTransactionService {

    private final InventoryTransactionRepository transactionRepository;

    // ==================== IMPORT OPERATIONS ====================

    @Override
    @Transactional
    public void logImportTransaction(Material material, int beforeQty, int afterQty,
                                     int reservedQty, int delta, BigDecimal newCostPrice,
                                     String referenceCode, String note) {
        InventoryTransaction tx = InventoryTransaction.builder()
                .material(material)
                .type(TransactionType.IMPORT)
                .quantityDelta(delta)              // Positive for import
                .beforeQuantity(beforeQty)
                .afterQuantity(afterQty)
                .currentBalance(afterQty)
                .beforeReserved(reservedQty)       // Import doesn't affect reserved
                .afterReserved(reservedQty)
                .costPrice(newCostPrice)
                .referenceCode(referenceCode)
                .note(note)
                .build();

        transactionRepository.save(tx);
        log.info("Logged IMPORT: material={}, delta=+{}, balance={}", 
                material.getId(), delta, afterQty);
    }

    // ==================== DEDUCTION OPERATIONS ====================

    @Override
    @Transactional
    public void logDeductTransaction(MaterialStock snapshot, int delta,
                                     TransactionType type, String referenceCode, String note) {
        int beforeQty = snapshot.getQuantity();
        int afterQty = beforeQty - delta;  // delta is positive, subtract for deduction

        InventoryTransaction tx = InventoryTransaction.builder()
                .material(snapshot.getMaterial())
                .type(type)
                .quantityDelta(-delta)             // Negative for deduction
                .beforeQuantity(beforeQty)
                .afterQuantity(afterQty)
                .currentBalance(afterQty)
                .beforeReserved(snapshot.getReservedQuantity())
                .afterReserved(snapshot.getReservedQuantity())  // No change to reserved
                .costPrice(snapshot.getCostPrice()) // Snapshot current MAC
                .referenceCode(referenceCode)
                .note(note)
                .build();

        transactionRepository.save(tx);
        log.info("Logged {}: material={}, delta=-{}, balance={}", 
                type, snapshot.getMaterial().getId(), delta, afterQty);
    }

    // ==================== ADJUSTMENT OPERATIONS ====================

    @Override
    @Transactional
    public void logAdjustmentTransaction(MaterialStock snapshot, int delta,
                                          TransactionType type, String note) {
        int beforeQty = snapshot.getQuantity();
        int afterQty = beforeQty + delta;  // delta is already signed

        InventoryTransaction tx = InventoryTransaction.builder()
                .material(snapshot.getMaterial())
                .type(type)
                .quantityDelta(delta)
                .beforeQuantity(beforeQty)
                .afterQuantity(afterQty)
                .currentBalance(afterQty)
                .beforeReserved(snapshot.getReservedQuantity())
                .afterReserved(snapshot.getReservedQuantity())
                .costPrice(snapshot.getCostPrice())
                .referenceCode(null)
                .note(note)
                .build();

        transactionRepository.save(tx);
        log.info("Logged {}: material={}, delta={}, balance={}", 
                type, snapshot.getMaterial().getId(), delta, afterQty);
    }

    // ==================== RESERVATION OPERATIONS ====================

    @Override
    @Transactional
    public void logReserveTransaction(MaterialStock snapshot, int reserveDelta, String referenceCode) {
        int beforeReserved = snapshot.getReservedQuantity();
        int afterReserved = beforeReserved + reserveDelta;

        InventoryTransaction tx = InventoryTransaction.builder()
                .material(snapshot.getMaterial())
                .type(TransactionType.RESERVE)
                .quantityDelta(0)                  // No change to actual quantity
                .beforeQuantity(snapshot.getQuantity())
                .afterQuantity(snapshot.getQuantity())
                .currentBalance(snapshot.getQuantity())
                .beforeReserved(beforeReserved)
                .afterReserved(afterReserved)
                .costPrice(snapshot.getCostPrice())
                .referenceCode(referenceCode)
                .note("Reserved for order: " + referenceCode)
                .build();

        transactionRepository.save(tx);
        log.info("Logged RESERVE: material={}, reserved={}->{}", 
                snapshot.getMaterial().getId(), beforeReserved, afterReserved);
    }

    @Override
    @Transactional
    public void logReleaseTransaction(MaterialStock snapshot, int releaseDelta,
                                       String referenceCode, String note) {
        int beforeReserved = snapshot.getReservedQuantity();
        int afterReserved = beforeReserved - releaseDelta;

        InventoryTransaction tx = InventoryTransaction.builder()
                .material(snapshot.getMaterial())
                .type(TransactionType.RELEASE)
                .quantityDelta(0)                  // No change to actual quantity
                .beforeQuantity(snapshot.getQuantity())
                .afterQuantity(snapshot.getQuantity())
                .currentBalance(snapshot.getQuantity())
                .beforeReserved(beforeReserved)
                .afterReserved(afterReserved)
                .costPrice(snapshot.getCostPrice())
                .referenceCode(referenceCode)
                .note(note)
                .build();

        transactionRepository.save(tx);
        log.info("Logged RELEASE: material={}, reserved={}->{}", 
                snapshot.getMaterial().getId(), beforeReserved, afterReserved);
    }

    @Override
    @Transactional
    public void logReturnTransaction(MaterialStock snapshot, int returnQty,
                                      String referenceCode, String note) {
        int beforeQty = snapshot.getQuantity();
        int afterQty = beforeQty + returnQty;

        InventoryTransaction tx = InventoryTransaction.builder()
                .material(snapshot.getMaterial())
                .type(TransactionType.RETURN)
                .quantityDelta(returnQty)          // Positive for return
                .beforeQuantity(beforeQty)
                .afterQuantity(afterQty)
                .currentBalance(afterQty)
                .beforeReserved(snapshot.getReservedQuantity())
                .afterReserved(snapshot.getReservedQuantity())
                .costPrice(snapshot.getCostPrice()) // Cost price unchanged for returns
                .referenceCode(referenceCode)
                .note(note)
                .build();

        transactionRepository.save(tx);
        log.info("Logged RETURN: material={}, delta=+{}, balance={}", 
                snapshot.getMaterial().getId(), returnQty, afterQty);
    }

    // ==================== QUERY OPERATIONS ====================

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryTransactionResponse> getTransactionHistory(Long materialId, Pageable pageable) {
        return transactionRepository.findByMaterialId(materialId, pageable)
                .map(this::toResponse);
    }

    // ==================== MAPPER ====================

    private InventoryTransactionResponse toResponse(InventoryTransaction tx) {
        return InventoryTransactionResponse.builder()
                .id(tx.getId())
                .materialId(tx.getMaterial().getId())
                .materialName(tx.getMaterial().getName())
                .type(tx.getType())
                .quantityDelta(tx.getQuantityDelta())
                .beforeQuantity(tx.getBeforeQuantity())
                .afterQuantity(tx.getAfterQuantity())
                .currentBalance(tx.getCurrentBalance())
                .beforeReserved(tx.getBeforeReserved())
                .afterReserved(tx.getAfterReserved())
                .costPrice(tx.getCostPrice())
                .referenceCode(tx.getReferenceCode())
                .note(tx.getNote())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}
