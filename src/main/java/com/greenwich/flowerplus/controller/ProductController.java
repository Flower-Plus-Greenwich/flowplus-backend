package com.greenwich.flowerplus.controller;

import com.greenwich.flowerplus.common.ApiResult;
import com.greenwich.flowerplus.dto.request.ProductSearchRequest;
import com.greenwich.flowerplus.dto.response.ProductListingDto;
import com.greenwich.flowerplus.dto.response.ProductResponse;
import com.greenwich.flowerplus.dto.response.ProductResponseAdmin;
import com.greenwich.flowerplus.service.ProductSearchService;
import com.greenwich.flowerplus.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;
    private final ProductSearchService productSearchService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<ProductResponse>> getProductById(@PathVariable Long id) {
        log.info("Received request to get product with id: {}", id);
        ProductResponse productResponse = productService.retrieveProductById(id);
        return ResponseEntity.ok(ApiResult.success(productResponse, "Product retrieved successfully"));
    }

    // ---------------------------------------------------------
    // PUBLIC API (Customer - Storefront)
    // - URL: GET /api/v1/products
    // - Forces ACTIVE status
    // - Returns ProductListingDto (lightweight)
    // ---------------------------------------------------------
    @GetMapping
    public ResponseEntity<ApiResult<List<ProductListingDto>>> searchPublic(
            @ModelAttribute ProductSearchRequest request) {
        log.info("Public search - keyword: {}, categoryId: {}, categorySlug: {}, sort: {}, page: {}, size: {}",
                request.getKeyword(), request.getCategoryId(), request.getCategorySlug(), request.getSort(), request.getPage(), request.getSize());

        Page<ProductListingDto> productPage = productSearchService.searchPublic(request);

        return ResponseEntity.ok(ApiResult.success(
                productPage.getContent(),
                request.getPage(),
                request.getSize(),
                productPage.getTotalElements()
        ));
    }

    // ---------------------------------------------------------
    // ADMIN API (Backoffice)
    // - URL: GET /api/v1/products/admin
    // - Supports status filtering or "get all" if no filters
    // - Returns ProductResponseAdmin (detailed)
    // - Requires OWNER, STAFF, ADMIN role
    // ---------------------------------------------------------
    @PreAuthorize("hasAnyRole('SHOP_OWNER', 'SHOP_STAFF', 'ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<ApiResult<List<ProductResponseAdmin>>> searchAdmin(
            @ModelAttribute ProductSearchRequest request) {
        log.info("Admin search - keyword: {}, categoryId: {}, categorySlug: {}, status: {}, page: {}, size: {}",
                request.getKeyword(), request.getCategoryId(), request.getCategorySlug(), request.getStatus(), request.getPage(), request.getSize());

        Page<ProductResponseAdmin> productPage = productSearchService.searchAdmin(request);

        return ResponseEntity.ok(ApiResult.success(
                productPage.getContent(),
                request.getPage(),
                request.getSize(),
                productPage.getTotalElements()
        ));
    }

}
