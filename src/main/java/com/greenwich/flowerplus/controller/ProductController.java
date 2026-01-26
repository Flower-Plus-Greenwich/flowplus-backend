package com.greenwich.flowerplus.controller;

import com.greenwich.flowerplus.common.ApiResult;
import com.greenwich.flowerplus.dto.request.ProductSearchRequest;
import com.greenwich.flowerplus.dto.response.ProductListingDto;
import com.greenwich.flowerplus.dto.response.ProductResponse;
import com.greenwich.flowerplus.service.ProductSearchService;
import com.greenwich.flowerplus.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
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

    @Operation(
            summary = "Lấy chi tiết sản phẩm (Public)",
            description = """
        Endpoint này dành cho xem chi tiết sản phẩm trên trang storefront.
        
        **Lưu ý:**
        - Chỉ trả về sản phẩm có status = ACTIVE
        - Nếu sản phẩm không ACTIVE sẽ trả về 404
        """
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<ProductResponse>> getProductById(@PathVariable Long id) {
        log.info("Received request to get product with id: {}", id);
        ProductResponse productResponse = productService.getProductForPublic(id);
        return ResponseEntity.ok(ApiResult.success(productResponse, "Product retrieved successfully"));
    }

    // ---------------------------------------------------------
    // PUBLIC API (Customer - Storefront)
    // - URL: GET /api/v1/products
    // - Forces ACTIVE status
    // - Returns ProductListingDto (lightweight)
    // ---------------------------------------------------------
    @Operation(
            summary = "Search sản phẩm công khai (Customer)", // Tiêu đề ngắn gọn hiện trên thanh bar
            description = """
        Endpoint này dành cho search công khai trên trang shop, trang chủ, front office.
        
        **Lưu ý:**
        - Hỗ trợ nhiều loại filter (giá, danh mục...).
        - KHÔNG cần truyền status. Backend mặc định filter status = ACTIVE.
        """
    )
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




}
