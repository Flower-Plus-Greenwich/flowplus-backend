package com.greenwich.flowerplus.controller;

import com.greenwich.flowerplus.common.ApiResult;
import com.greenwich.flowerplus.dto.request.CreateGeneralInfoProductRequest;
import com.greenwich.flowerplus.dto.request.ProductAssetRequest;
import com.greenwich.flowerplus.dto.request.ProductCategoryRequest;
import com.greenwich.flowerplus.dto.request.ProductSearchRequest;
import com.greenwich.flowerplus.dto.request.UpdateProductInfoRequest;
import com.greenwich.flowerplus.dto.response.ProductResponseAdmin;
import com.greenwich.flowerplus.service.ProductSearchService;
import com.greenwich.flowerplus.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/products")
@Tag(name = "Admin: Product Management", description = "API quản lý sản phẩm (Backoffice)")
public class AdminProductController {

    private final ProductService productService;
    private final ProductSearchService productSearchService;

    // ============================================================================
    // SEARCH & RETRIEVE
    // ============================================================================

    @Operation(
            summary = "Search sản phẩm bên phía Admin",
            description = """
        Endpoint này dành cho search cho admin trên trang dashboard, quản lí products.
        
        **Lưu ý:**
        - Hỗ trợ nhiều loại filter (giá, danh mục...).
        - CẦN truyền status. Vì bên admin còn xem đc draft với các trạng thái khác nữa
        - STATUS gồm : DRAFT, ACTIVE, INACTIVE, ARCHIVED
        """
    )
    @PreAuthorize("hasAnyAuthority('ROLE_SHOP_OWNER', 'ROLE_SHOP_STAFF', 'ROLE_ADMIN')")
    @GetMapping
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

    @Operation(summary = "API lấy full information product", description = """
        Endpoint này dành cho việc lấy full thông tin sản phẩm gồm categories, assets, tạo bởi ai, ngày giờ tạo
        
        [role staff, owner, admin]
        """)
    @PreAuthorize("hasAnyAuthority('ROLE_SHOP_OWNER', 'ROLE_SHOP_STAFF', 'ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<ProductResponseAdmin>> getProductById(@PathVariable Long id) {
        log.info("Received request to get product with id: {}", id);
        ProductResponseAdmin productResponse = productService.getProductById(id);
        return ResponseEntity.ok(ApiResult.success(productResponse, "Product retrieved successfully"));
    }

    // ============================================================================
    // CREATE & UPDATE GENERAL INFO
    // ============================================================================

    @Operation(summary = "API tạo 1 sản phẩm với trạng thái DRAFT", description = """
        Endpoint này dành cho việc tạo product [role staff, owner, admin]
        
        **Lưu ý:**
        - Tạo product chỉ bao gồm field cơ bản gồm tên, description, basePrice (selling price), category id
        - STATUS luôn tạo ra với mặc định giá trị là DRAFT
        - Tên sản phẩm sẽ được validate: không chứa từ ngữ không phù hợp, ký tự đặc biệt
        """)
    @PreAuthorize("hasAnyAuthority('ROLE_SHOP_OWNER', 'ROLE_SHOP_STAFF', 'ROLE_ADMIN')")
    @PostMapping("/draft")
    public ResponseEntity<ApiResult<ProductResponseAdmin>> createDraftProduct(
            @Valid @RequestBody CreateGeneralInfoProductRequest request) {
        log.info("Received request to create draft product: {}", request.name());
        ProductResponseAdmin productResponse = productService.createGeneralInfoProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success(productResponse, "Product created successfully"));
    }

    @Operation(summary = "API cập nhật thông tin chung của sản phẩm", description = """
        Endpoint này dành cho update thông tin chung của product [role staff, owner, admin]
        
        **Các field có thể update:**
        - name, description, careInstruction, basePrice
        - weight, length, width, height (shipping info)
        - assets (ảnh/video)
        - isMakeToOrder
        
        **Lưu ý:**
        - Không thể update slug (URL-friendly)
        - Tất cả text sẽ được validate bad words
        """)
    @PreAuthorize("hasAnyAuthority('ROLE_SHOP_OWNER', 'ROLE_SHOP_STAFF', 'ROLE_ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResult<ProductResponseAdmin>> updateGeneralInfoProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductInfoRequest updateProductRequest) {
        log.info("Received request to update general info product with id: {}", id);
        ProductResponseAdmin productResponse = productService.updateProduct(id, updateProductRequest);
        return ResponseEntity.ok(ApiResult.success(productResponse, "Product updated successfully"));
    }

    // ============================================================================
    // MANAGE ASSETS (Single Responsibility)
    // ============================================================================

    @Operation(summary = "API quản lý assets (ảnh/video) của sản phẩm", description = """
        Endpoint này dành cho quản lý assets riêng biệt [role staff, owner, admin]
        
        **Operations:**
        - `ADD`: Thêm assets mới vào danh sách hiện có (max 10 assets)
        - `REMOVE`: Xóa assets theo ID
        - `REPLACE`: Thay thế toàn bộ assets bằng danh sách mới
        - `SET_THUMBNAIL`: Đặt 1 asset làm thumbnail
        - `REORDER`: Sắp xếp lại thứ tự hiển thị
        
        **Validation:**
        - URL phải là http/https hợp lệ
        - Không được duplicate URL trong cùng product
        - Maximum 10 assets per product
        """)
    @PreAuthorize("hasAnyAuthority('ROLE_SHOP_OWNER', 'ROLE_SHOP_STAFF', 'ROLE_ADMIN')")
    @PutMapping("/{id}/assets")
    public ResponseEntity<ApiResult<ProductResponseAdmin>> manageProductAssets(
            @PathVariable Long id,
            @Valid @RequestBody ProductAssetRequest request) {
        log.info("Received request to manage assets for product id: {}, operation: {}", id, request.operation());
        ProductResponseAdmin productResponse = productService.manageProductAssets(id, request);
        return ResponseEntity.ok(ApiResult.success(productResponse, "Product assets updated successfully"));
    }

    // ============================================================================
    // MANAGE CATEGORIES (Single Responsibility)
    // ============================================================================

    @Operation(summary = "API quản lý categories của sản phẩm", description = """
        Endpoint này dành cho quản lý categories riêng biệt [role staff, owner, admin]
        
        **Operations:**
        - `ADD`: Thêm categories mới (không duplicate)
        - `REMOVE`: Xóa categories theo ID (phải còn ít nhất 1)
        - `REPLACE`: Thay thế toàn bộ categories
        - `CLEAR`: Không được phép (product phải có ít nhất 1 category)
        
        **Business Rules:**
        - Product phải có ít nhất 1 category
        - Maximum 5 categories per product
        - Category phải active mới được gán
        """)
    @PreAuthorize("hasAnyAuthority('ROLE_SHOP_OWNER', 'ROLE_SHOP_STAFF', 'ROLE_ADMIN')")
    @PutMapping("/{id}/categories")
    public ResponseEntity<ApiResult<ProductResponseAdmin>> manageProductCategories(
            @PathVariable Long id,
            @Valid @RequestBody ProductCategoryRequest request) {
        log.info("Received request to manage categories for product id: {}, operation: {}", id, request.operation());
        ProductResponseAdmin productResponse = productService.manageProductCategories(id, request);
        return ResponseEntity.ok(ApiResult.success(productResponse, "Product categories updated successfully"));
    }

    // ============================================================================
    // DELETE
    // ============================================================================

    @Operation(summary = "API xóa sản phẩm (soft delete)", description = """
        Endpoint này dành cho xóa product [role staff, owner, admin]
        
        **Lưu ý:**
        - Soft delete: chỉ đánh dấu deleted_at, không xóa thật
        """)
    @PreAuthorize("hasAnyAuthority('ROLE_SHOP_OWNER', 'ROLE_SHOP_STAFF', 'ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResult<Void>> removeProduct(@PathVariable Long id) {
        log.info("Received request to remove product with id: {}", id);
        productService.removeProduct(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResult.success(null, "Product removed successfully"));
    }
}
