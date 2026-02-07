package com.greenwich.flowerplus.controller;

import com.greenwich.flowerplus.common.ApiResult;
import com.greenwich.flowerplus.dto.request.CategoryRequest;
import com.greenwich.flowerplus.dto.response.CategoryResponse;
import com.greenwich.flowerplus.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin Category Controller for Backoffice operations.
 * Provides full CRUD operations for managing categories and collections.
 */
@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/categories")
@Tag(name = "Category Management (Admin)", description = "APIs for managing categories and marketing collections")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @Operation(
            summary = "Get all categories (Admin)",
            description = """
                Returns ALL categories including inactive ones.
                Use this for admin management dashboard.
                """
    )
    @PreAuthorize("hasAnyAuthority('ROLE_SHOP_OWNER', 'ROLE_SHOP_STAFF', 'ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResult<List<CategoryResponse>>> getAllCategories() {
        log.info("Admin: Retrieving all categories");
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResult.success(categories));
    }

    @Operation(
            summary = "Get category by ID (Admin)",
            description = "Returns full category details including inactive status"
    )
    @PreAuthorize("hasAnyAuthority('ROLE_SHOP_OWNER', 'ROLE_SHOP_STAFF', 'ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<CategoryResponse>> getCategoryById(@PathVariable Long id) {
        log.info("Admin: Getting category with id: {}", id);
        CategoryResponse category = categoryService.getCategory(id);
        return ResponseEntity.ok(ApiResult.success(category));
    }

    @Operation(
            summary = "Create a new category",
            description = """
                Creates a new category or marketing collection.
                
                **Notes:**
                - Slug is auto-generated from name
                - Name must be unique
                - Set isActive=false for draft/upcoming collections
                """
    )
    @PreAuthorize("hasAnyAuthority('ROLE_SHOP_OWNER', 'ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResult<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        log.info("Admin: Creating category with name: {}", request.getName());
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success(response, "Category created successfully"));
    }

    @Operation(
            summary = "Update an existing category",
            description = """
                Updates category/collection details.
                
                **Notes:**
                - Slug cannot be changed (SEO-friendly URLs)
                - Toggle isActive to show/hide seasonal collections
                """
    )
    @PreAuthorize("hasAnyAuthority('ROLE_SHOP_OWNER', 'ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResult<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        log.info("Admin: Updating category with id: {}", id);
        CategoryResponse response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResult.success(response, "Category updated successfully"));
    }

    @Operation(
            summary = "Delete a category (soft delete)",
            description = """
                Soft deletes a category.
                
                **Validation:**
                - Cannot delete category with child categories
                - Soft delete preserves data integrity
                """
    )
    @PreAuthorize("hasAnyAuthority('ROLE_SHOP_OWNER', 'ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResult<Void>> deleteCategory(@PathVariable Long id) {
        log.info("Admin: Deleting category with id: {}", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResult.success(null, "Category deleted successfully"));
    }
}
