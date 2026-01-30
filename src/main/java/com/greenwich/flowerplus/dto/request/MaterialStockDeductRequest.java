package com.greenwich.flowerplus.dto.request;

import com.greenwich.flowerplus.common.enums.TransactionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for deducting materials from stock.
 * Used for sales, usage (production), or marking damaged materials.
 * Note: Deduction does NOT change the cost price, only the quantity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialStockDeductRequest {

    @NotNull(message = "Material ID is required")
    private Long materialId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    /**
     * Type of deduction: USAGE, DAMAGED, etc.
     * Note: IMPORT type is not allowed here, use MaterialStockImportRequest instead.
     */
    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    /**
     * Reference code for traceability (e.g., Order ID for sales)
     */
    private String referenceCode;

    /**
     * Optional note for this deduction transaction (e.g., "Damaged during storage")
     */
    private String note;
}
