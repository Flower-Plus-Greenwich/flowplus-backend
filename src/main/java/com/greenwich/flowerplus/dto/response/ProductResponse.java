package com.greenwich.flowerplus.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.greenwich.flowerplus.common.enums.ProductStatus;
import com.greenwich.flowerplus.dto.snapshot.CategorySnapshot;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

/**
 * ProductResponse - Customer-facing product details
 * 
 * Used for:
 * - Product detail page (public)
 * - Basic product operations responses
 * 
 * This DTO reflects the many-to-many relationship between Product and Category
 * through ProductCategory entity, showing:
 * - primaryCategory: The first/main category
 * - categories: All categories the product belongs to
 */
@Builder
public record ProductResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long id,
        
        String name,
        String slug,
        String description,
        String careInstruction,
        
        BigDecimal basePrice,
        
        // Primary category (first category) - for backward compatibility and display
        CategorySnapshot primaryCategory,
        
        // All categories - reflects the many-to-many relationship
        List<CategorySnapshot> categories,
        
        ProductStatus status,
        
        // Assets
        List<AssetResponse> assets,
        String thumbnail,
        
        // Physical attributes (shipping info)
        Integer weight,
        Integer length,
        Integer width,
        Integer height,
        
        // Stock info
        Integer preparedQuantity,
        boolean isMakeToOrder,
        boolean inStock,
        
        // Rating
        Double averageRating,
        Integer reviewCount
) {
}
