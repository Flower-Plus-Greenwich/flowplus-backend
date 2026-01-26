package com.greenwich.flowerplus.service;

import com.greenwich.flowerplus.dto.request.CreateGeneralInfoProductRequest;
import com.greenwich.flowerplus.dto.request.ProductAssetRequest;
import com.greenwich.flowerplus.dto.request.ProductCategoryRequest;
import com.greenwich.flowerplus.dto.request.UpdateProductInfoRequest;
import com.greenwich.flowerplus.dto.request.UpdateProductStatusRequest;
import com.greenwich.flowerplus.dto.response.ProductResponse;
import com.greenwich.flowerplus.dto.response.ProductResponseAdmin;

/**
 * ProductService - Single Responsibility: Product CRUD Operations
 * 
 * Handles:
 * - Create, Update, Delete products
 * - Manage product assets
 * - Manage product categories
 * - Get product by ID (for admin/detail view)
 * 
 * NOTE: Search operations are in ProductSearchService (SRP)
 */
public interface ProductService {

    // ============ RETRIEVE (Single Product) ============
    /**
     * Get product by ID for admin/backoffice (full details)
     */
    ProductResponseAdmin getProductById(Long id);

    /**
     * Get product by ID for public/storefront
     */
    ProductResponse getProductForPublic(Long id);

    // ============ CREATE ============
    ProductResponse createGeneralInfoProduct(CreateGeneralInfoProductRequest request);

    // ============ UPDATE GENERAL INFO ============
    ProductResponse updateProduct(Long id, UpdateProductInfoRequest request);
    ProductResponse updateProductStatus(Long id, UpdateProductStatusRequest request);

    // ============ MANAGE ASSETS (Single Responsibility) ============
    /**
     * Manage product assets with operations: ADD, REMOVE, REPLACE, SET_THUMBNAIL, REORDER
     */
    ProductResponseAdmin manageProductAssets(Long productId, ProductAssetRequest request);

    // ============ MANAGE CATEGORIES (Single Responsibility) ============
    /**
     * Manage product categories with operations: ADD, REMOVE, REPLACE, CLEAR
     */
    ProductResponseAdmin manageProductCategories(Long productId, ProductCategoryRequest request);

    // ============ DELETE ============
    void removeProduct(Long id);
}
