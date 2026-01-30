package com.greenwich.flowerplus.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating stock settings like reorder level.
 * Includes version for optimistic locking to prevent lost updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialStockUpdateRequest {

    @NotNull(message = "Reorder level is required")
    @Min(value = 0, message = "Reorder level must be non-negative")
    private Integer reorderLevel;

    /**
     * Version from the last read operation.
     * Required for optimistic locking to detect concurrent modifications.
     */
    @NotNull(message = "Version is required for optimistic locking")
    private Long version;
}
