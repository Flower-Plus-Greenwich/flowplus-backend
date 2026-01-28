package com.greenwich.flowerplus.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for importing materials into stock.
 * This triggers MAC (Moving Average Cost) calculation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialStockImportRequest {

    @NotNull(message = "Material ID is required")
    private Long materialId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Import price is required")
    @DecimalMin(value = "0.01", message = "Import price must be greater than 0")
    private BigDecimal importPrice;

    /**
     * Reference code for traceability (e.g., Import Batch Code, Supplier Invoice Number)
     */
    private String referenceCode;

    /**
     * Optional note for this import transaction
     */
    private String note;
}
