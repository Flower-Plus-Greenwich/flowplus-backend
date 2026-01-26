package com.greenwich.flowerplus.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request DTO to manage product categories.
 * Supports add, remove, and replace operations.
 */
public record ProductCategoryRequest(

        @Schema(description = "Operation type: ADD, REMOVE, REPLACE", example = "ADD")
        @NotNull(message = "Operation type is required")
        CategoryOperation operation,

        @Schema(description = "List of category IDs to process", example = "[\"792254090050729589\", \"792254090050729590\"]")
        @NotEmpty(message = "Category IDs cannot be empty")
        List<Long> categoryIds

) {

    public enum CategoryOperation {
        /**
         * Add categories to existing ones (no duplicates)
         */
        ADD,
        
        /**
         * Remove specific categories from product
         */
        REMOVE,
        
        /**
         * Replace all categories with the provided list
         */
        REPLACE,
        
        /**
         * Clear all categories from product
         */
        CLEAR
    }
}
