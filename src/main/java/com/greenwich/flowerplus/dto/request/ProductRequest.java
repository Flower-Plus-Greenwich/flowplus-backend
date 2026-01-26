package com.greenwich.flowerplus.dto.request;

import java.math.BigDecimal;
import java.util.List;

/**
 * Common interface for product request DTOs.
 * Defines fields that are common across all product operations.
 */
public interface ProductRequest {
    String name();
    String description();
    BigDecimal basePrice();
    String slug();
    default List<AssetRequest> assets() {
        return null;
    }
}
