package com.greenwich.flowerplus.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO showing current stock state for a material.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialStockResponse {

    private Long materialId;
    private String materialName;
    private String unit;

    /**
     * Total quantity in stock
     */
    private Integer quantity;

    /**
     * Quantity reserved for pending orders
     */
    private Integer reservedQuantity;

    /**
     * Available stock = quantity - reservedQuantity
     */
    private Integer availableStock;

    /**
     * Moving Average Cost (MAC) - unit cost price
     */
    private BigDecimal costPrice;

    /**
     * Total inventory value = costPrice * quantity
     */
    private BigDecimal totalValue;

    /**
     * Threshold for low stock warning
     */
    private Integer reorderLevel;

    /**
     * True if availableStock <= reorderLevel
     */
    private boolean lowStock;

    /**
     * Version for optimistic locking (send this back when updating)
     */
    private Long version;
}
