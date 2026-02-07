package com.greenwich.flowerplus.service;

import com.greenwich.flowerplus.dto.request.CategoryRequest;
import com.greenwich.flowerplus.dto.response.CategoryPublicResponse;
import com.greenwich.flowerplus.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    /**
     * Create a new category with auto-generated slug.
     */
    CategoryResponse createCategory(CategoryRequest request);

    /**
     * Update an existing category.
     */
    CategoryResponse updateCategory(Long id, CategoryRequest request);

    /**
     * Get a single category by ID.
     */
    CategoryResponse getCategory(Long id);

    /**
     * Get all categories (Admin only).
     */
    List<CategoryResponse> getAllCategories();

    /**
     * Get only active categories for public display.
     * Returns lightweight DTO without audit fields.
     */
    List<CategoryPublicResponse> getActiveCategories();

    /**
     * Get a single active category for public display.
     */
    CategoryPublicResponse getCategoryForPublic(Long id);

    /**
     * Soft delete a category after validation.
     */
    void deleteCategory(Long id);
}
