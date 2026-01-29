package com.greenwich.flowerplus.controller;

import com.greenwich.flowerplus.common.ApiResult;
import com.greenwich.flowerplus.dto.request.MaterialStockDeductRequest;
import com.greenwich.flowerplus.dto.request.MaterialStockImportRequest;
import com.greenwich.flowerplus.dto.request.MaterialStockUpdateRequest;
import com.greenwich.flowerplus.dto.response.InventoryTransactionResponse;
import com.greenwich.flowerplus.dto.response.MaterialStockResponse;
import com.greenwich.flowerplus.service.MaterialStockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Material Stock Management.
 * Handles inventory operations with:
 * - Moving Average Cost (MAC) calculation on imports
 * - Pessimistic locking for financial integrity
 * - Transaction ledger for audit trails
 */
@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/materials")
@Tag(name = "Material Stock Management", description = "APIs for managing material inventory and stock")
public class MaterialStockController {

    private final MaterialStockService materialStockService;

    // ==================== FINANCIAL OPERATIONS (Pessimistic Lock) ====================

    @Operation(
            summary = "Import materials into stock",
            description = "Import materials with automatic MAC (Moving Average Cost) calculation. " +
                    "Uses pessimistic locking to ensure data integrity during concurrent imports."
    )
    @PreAuthorize("hasAnyAuthority('ROLE_SHOP_OWNER', 'ROLE_SHOP_STAFF', 'ROLE_ADMIN')")
    @PostMapping("/stocks/import")
    public ResponseEntity<ApiResult<MaterialStockResponse>> importStock(
            @Valid @RequestBody MaterialStockImportRequest request) {
        log.info("Import stock request: materialId={}, qty={}, price={}",
                request.getMaterialId(), request.getQuantity(), request.getImportPrice());

        MaterialStockResponse response = materialStockService.importStock(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success(response, "Stock imported successfully"));
    }

    @Operation(
            summary = "Deduct materials from stock",
            description = "Deduct materials for usage, damage, or other reasons. " +
                    "Creates a transaction log with cost price snapshot for audit trail."
    )
    @PreAuthorize("hasAnyAuthority('ROLE_SHOP_OWNER', 'ROLE_SHOP_STAFF', 'ROLE_ADMIN')")
    @PostMapping("/stocks/deduct")
    public ResponseEntity<ApiResult<MaterialStockResponse>> deductStock(
            @Valid @RequestBody MaterialStockDeductRequest request) {
        log.info("Deduct stock request: materialId={}, qty={}, type={}",
                request.getMaterialId(), request.getQuantity(), request.getType());

        MaterialStockResponse response = materialStockService.deductStock(request);
        return ResponseEntity.ok(ApiResult.success(response, "Stock deducted successfully"));
    }

    // ==================== READ OPERATIONS ====================

    @Operation(summary = "Get current stock information for a material")
    @PreAuthorize("hasAnyAuthority('ROLE_SHOP_OWNER', 'ROLE_SHOP_STAFF', 'ROLE_ADMIN')")
    @GetMapping("/{materialId}/stock")
    public ResponseEntity<ApiResult<MaterialStockResponse>> getStock(
            @Parameter(description = "Material ID") @PathVariable Long materialId) {
        log.info("Get stock request: materialId={}", materialId);

        MaterialStockResponse response = materialStockService.getStockByMaterialId(materialId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(
            summary = "Get transaction history for a material",
            description = "Returns paginated list of all inventory transactions (imports, usage, damage, etc.) " +
                    "for a material, ordered by newest first."
    )
    @PreAuthorize("hasAnyAuthority('ROLE_SHOP_OWNER', 'ROLE_SHOP_STAFF', 'ROLE_ADMIN')")
    @GetMapping("/{materialId}/transactions")
    public ResponseEntity<ApiResult<List<InventoryTransactionResponse>>> getTransactionHistory(
            @Parameter(description = "Material ID") @PathVariable Long materialId,
            @Parameter(description = "Page number (1-indexed)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        log.info("Get transaction history: materialId={}, page={}, size={}", materialId, page, size);

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<InventoryTransactionResponse> result = materialStockService.getTransactionHistory(materialId, pageable);

        return ResponseEntity.ok(ApiResult.success(
                result.getContent(),
                page,
                size,
                result.getTotalElements()
        ));
    }

    // ==================== UI/SETTINGS OPERATIONS (Optimistic Lock) ====================

    @Operation(
            summary = "Update stock settings (reorder level)",
            description = "Update stock configuration. Uses optimistic locking to prevent lost updates " +
                    "when multiple users edit simultaneously."
    )
    @PreAuthorize("hasAnyAuthority('ROLE_SHOP_OWNER', 'ROLE_SHOP_STAFF', 'ROLE_ADMIN')")
    @PatchMapping("/{materialId}/stock")
    public ResponseEntity<ApiResult<MaterialStockResponse>> updateStockSettings(
            @Parameter(description = "Material ID") @PathVariable Long materialId,
            @Valid @RequestBody MaterialStockUpdateRequest request) {
        log.info("Update stock settings: materialId={}, reorderLevel={}, version={}",
                materialId, request.getReorderLevel(), request.getVersion());

        MaterialStockResponse response = materialStockService.updateReorderLevel(
                materialId, request.getReorderLevel(), request.getVersion());
        return ResponseEntity.ok(ApiResult.success(response, "Stock settings updated successfully"));
    }

    // ==================== ADMIN OPERATIONS ====================

    @Operation(
            summary = "Initialize stock for a material",
            description = "Create initial stock record for a new material. Usually called automatically when creating a material."
    )
    @PreAuthorize("hasAnyAuthority('ROLE_SHOP_OWNER', 'ROLE_ADMIN')")
    @PostMapping("/{materialId}/stock/initialize")
    public ResponseEntity<ApiResult<MaterialStockResponse>> initializeStock(
            @Parameter(description = "Material ID") @PathVariable Long materialId) {
        log.info("Initialize stock request: materialId={}", materialId);

        MaterialStockResponse response = materialStockService.initializeStock(materialId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success(response, "Stock initialized successfully"));
    }

    @Operation(
            summary = "Manual stock adjustment",
            description = "Adjust stock quantity up or down for inventory audit purposes. Requires a justification note."
    )
    @PreAuthorize("hasAnyAuthority('ROLE_SHOP_OWNER', 'ROLE_ADMIN')")
    @PostMapping("/{materialId}/stock/adjust")
    public ResponseEntity<ApiResult<MaterialStockResponse>> adjustStock(
            @Parameter(description = "Material ID") @PathVariable Long materialId,
            @Parameter(description = "Adjustment amount (positive or negative)") @RequestParam int delta,
            @Parameter(description = "Justification note") @RequestParam String note) {
        log.info("Adjust stock request: materialId={}, delta={}", materialId, delta);

        MaterialStockResponse response = materialStockService.adjustStock(materialId, delta, note);
        return ResponseEntity.ok(ApiResult.success(response, "Stock adjusted successfully"));
    }

    // ==================== SALES OPERATIONS (High Concurrency - Atomic Update) ====================
    @Operation(
            summary = "Deduct stock for order (High Concurrency)",
            description = "Atomic deduction optimized for high-concurrency sales. Does not use database locks. *(API này chỉ dành cho staff thôi frontend làm ơn chú ý là ko dùng cái này nhé để thực hiện order cho staff)"
    )
    @PreAuthorize("hasAnyAuthority('ROLE_SHOP_OWNER', 'ROLE_SHOP_STAFF', 'ROLE_ADMIN')")
    @PostMapping("/{materialId}/stock/deduct-for-order")
    public ResponseEntity<ApiResult<Void>> deductForOrder(
            @Parameter(description = "Material ID") @PathVariable Long materialId,
            @Parameter(description = "Quantity to deduct") @RequestParam int quantity,
            @Parameter(description = "Order reference code") @RequestParam String orderCode) {
        log.info("Deduct for order: materialId={}, qty={}, order={}", materialId, quantity, orderCode);

        materialStockService.deductForOrder(materialId, quantity, orderCode);
        return ResponseEntity.ok(ApiResult.success(null, "Stock deducted for order"));
    }


    // ======================================================================
    // SUPPORT OPERATIONS (Dành cho Staff xử lý đơn ngoài luồng/Chat)
    // Lưu ý: KHÔNG dùng API này cho luồng Checkout tự động trên Website.
    // ======================================================================
    @Operation(
            summary = "Reserve stock for pending order",
            description = "Reserve stock for a pending order. Reserved stock is not available for other orders."
    )
    @PreAuthorize("hasAnyAuthority('ROLE_SHOP_OWNER', 'ROLE_SHOP_STAFF', 'ROLE_ADMIN')")
    @PostMapping("/{materialId}/stock/reserve")
    public ResponseEntity<ApiResult<Void>> reserveStock(
            @Parameter(description = "Material ID") @PathVariable Long materialId,
            @Parameter(description = "Quantity to reserve") @RequestParam int quantity,
            @Parameter(description = "Order reference code") @RequestParam String orderCode) {
        log.info("Reserve stock: materialId={}, qty={}, order={}", materialId, quantity, orderCode);

        materialStockService.reserveForOrder(materialId, quantity, orderCode);
        return ResponseEntity.ok(ApiResult.success(null, "Stock reserved for order"));
    }


    // ======================================================================
    // SUPPORT OPERATIONS (Dành cho Staff xử lý đơn ngoài luồng/Chat)
    // Lưu ý: KHÔNG dùng API này cho luồng Checkout tự động trên Website.
    // ======================================================================
    @Operation(
            summary = "Release reserved stock",
            description = "Release previously reserved stock (e.g., when order is cancelled)."
    )
    @PreAuthorize("hasAnyAuthority('ROLE_SHOP_OWNER', 'ROLE_SHOP_STAFF', 'ROLE_ADMIN')")
    @PostMapping("/{materialId}/stock/release")
    public ResponseEntity<ApiResult<Void>> releaseStock(
            @Parameter(description = "Material ID") @PathVariable Long materialId,
            @Parameter(description = "Quantity to release") @RequestParam int quantity,
            @Parameter(description = "Order reference code") @RequestParam String orderCode,
            @Parameter(description = "Reason for release") @RequestParam String reason) {
        log.info("Release stock: materialId={}, qty={}, order={}", materialId, quantity, orderCode);

        materialStockService.releaseReservation(materialId, quantity, orderCode, reason);
        return ResponseEntity.ok(ApiResult.success(null, "Reserved stock released"));
    }

    @Operation(
            summary = "Confirm reservation and deduct stock",
            description = "Confirm reserved stock when order ships. Converts reservation to actual deduction."
    )
    @PreAuthorize("hasAnyAuthority('ROLE_SHOP_OWNER', 'ROLE_SHOP_STAFF', 'ROLE_ADMIN')")
    @PostMapping("/{materialId}/stock/confirm")
    public ResponseEntity<ApiResult<Void>> confirmReservation(
            @Parameter(description = "Material ID") @PathVariable Long materialId,
            @Parameter(description = "Quantity to confirm") @RequestParam int quantity,
            @Parameter(description = "Order reference code") @RequestParam String orderCode) {
        log.info("Confirm reservation: materialId={}, qty={}, order={}", materialId, quantity, orderCode);

        materialStockService.confirmReservation(materialId, quantity, orderCode);
        return ResponseEntity.ok(ApiResult.success(null, "Reservation confirmed"));
    }
}
