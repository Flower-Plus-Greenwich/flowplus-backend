package com.greenwich.flowerplus.dto.request;

import com.greenwich.flowerplus.common.enums.ProductSort;
import com.greenwich.flowerplus.common.enums.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * ProductSearchRequest - Request DTO for product search/filter
 * 
 * Supports:
 * - Keyword search
 * - Multiple category filtering (OR logic - product in ANY of the selected categories)
 * - Price range filtering
 * - Status filtering (admin only)
 * - Sorting and pagination
 */
@Getter
@Setter
public class ProductSearchRequest {

    @Schema(description = "Search keyword for product name", example = "hoa hồng")
    private String keyword;

    // ============================================================================
    // CATEGORY FILTERS - Support multiple selection
    // ============================================================================

    @Schema(description = "Single category ID (legacy support)", example = "792254090050729589")
    private String categoryId;

    @Schema(description = "Single category slug (legacy support)", example = "hoa-sinh-nhat")
    private String categorySlug;

    @Schema(description = "List of category IDs for multi-select filter (OR logic)", 
            example = "[\"792254090050729589\", \"792254090050729590\"]")
    private List<String> categoryIds;

    @Schema(description = "List of category slugs for multi-select filter (OR logic)", 
            example = "[\"hoa-sinh-nhat\", \"hoa-cuoi\"]")
    private List<String> categorySlugs;

    // ============================================================================
    // PRICE FILTERS
    // ============================================================================

    @Schema(description = "Minimum price filter", example = "100000")
    private BigDecimal minPrice;

    @Schema(description = "Maximum price filter", example = "500000")
    private BigDecimal maxPrice;

    // ============================================================================
    // STATUS FILTER (Admin only)
    // ============================================================================

    @Schema(description = "Product status filter (DRAFT, ACTIVE, INACTIVE, ARCHIVED)")
    private ProductStatus status;

    // ============================================================================
    // PAGINATION & SORTING
    // ============================================================================

    @Schema(description = "Page number (1-indexed)", example = "1")
    private int page = 1;

    @Schema(description = "Page size", example = "20")
    private int size = 20;

    @Schema(description = "Sort option", example = "NEWEST")
    private ProductSort sort = ProductSort.NEWEST;

    // ============================================================================
    // HELPER METHODS
    // ============================================================================

    /**
     * Check if any category filter is applied
     */
    public boolean hasCategoryFilter() {
        return (categoryId != null && !categoryId.isBlank()) 
                || (categorySlug != null && !categorySlug.isBlank())
                || (categoryIds != null && !categoryIds.isEmpty())
                || (categorySlugs != null && !categorySlugs.isEmpty());
    }

    /**
     * Check if multiple categories are selected
     */
    public boolean hasMultipleCategoryFilter() {
        return (categoryIds != null && !categoryIds.isEmpty())
                || (categorySlugs != null && !categorySlugs.isEmpty());
    }

    @AssertTrue(message = "minPrice must be <= maxPrice")
    public boolean isValidPriceRange() {
        if (minPrice == null || maxPrice == null) return true;
        return minPrice.compareTo(maxPrice) <= 0;
    }
}