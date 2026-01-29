package com.greenwich.flowerplus.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.greenwich.flowerplus.dto.snapshot.CategorySnapshot;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * ProductListingDto - Lightweight DTO for product search results
 * 
 * Used for:
 * - Storefront product listing/grid
 * - Search results
 * - Category page products
 * 
 * Optimized for performance with minimal data for list views.
 * Supports multiple categories via CategorySnapshot list.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListingDto {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String name;
    private String slug;

    // Base price for display (Frontend can add "From..." if needed)
    private BigDecimal price;

    // Primary category name for quick display/labels
    private String categoryName;
    
    // All categories - for filtering UI and category badges
    private List<CategorySnapshot> categories;

    // Thumbnail image URL
    private String thumbnail;

    // Stock information
    private boolean inStock;
    private Boolean isSeasonalPriority;
    
    // Rating info (optional for display)
    private Double averageRating;
    private Integer reviewCount;
}