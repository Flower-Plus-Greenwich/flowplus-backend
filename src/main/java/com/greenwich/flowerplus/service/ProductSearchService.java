package com.greenwich.flowerplus.service;

import com.greenwich.flowerplus.dto.request.ProductSearchRequest;
import com.greenwich.flowerplus.dto.response.ProductListingDto;
import com.greenwich.flowerplus.dto.response.ProductResponseAdmin;
import org.springframework.data.domain.Page;

public interface ProductSearchService {
    /**
     * Public search - forces ACTIVE status only
     * Used by customer-facing storefront
     */
    Page<ProductListingDto> searchPublic(ProductSearchRequest req);

    /**
     * Admin search - respects status parameter
     * If no filters provided, returns all products with pagination
     */
    Page<ProductResponseAdmin> searchAdmin(ProductSearchRequest req);
}
