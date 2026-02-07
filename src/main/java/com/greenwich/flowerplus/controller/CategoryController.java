package com.greenwich.flowerplus.controller;

import com.greenwich.flowerplus.common.ApiResult;
import com.greenwich.flowerplus.dto.response.CategoryPublicResponse;
import com.greenwich.flowerplus.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public Category Controller for Storefront.
 * Provides read-only access to active categories for navigation and filtering.
 */
@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories (Public)", description = "Public APIs for browsing categories and collections")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(
            summary = "Get all active categories (Public)",
            description = """
                Returns ONLY active categories for storefront display.
                
                **Use cases:**
                - Category navigation sidebar
                - Product filter dropdowns
                - Collection showcase sections
                
                **Response includes:**
                - slug: For SEO-friendly URLs and search filters
                - thumbnail: Visual representation for category cards
                - children: Nested subcategories
                """
    )
    @GetMapping
    public ResponseEntity<ApiResult<List<CategoryPublicResponse>>> getActiveCategories() {
        log.info("Public: Retrieving active categories");
        List<CategoryPublicResponse> categories = categoryService.getActiveCategories();
        return ResponseEntity.ok(ApiResult.success(categories));
    }

    @Operation(
            summary = "Get category by ID (Public)",
            description = """
                Returns a single active category by ID.
                
                **Note:** Returns 404 if category is inactive or deleted.
                """
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<CategoryPublicResponse>> getCategoryById(@PathVariable Long id) {
        log.info("Public: Getting category with id: {}", id);
        CategoryPublicResponse category = categoryService.getCategoryForPublic(id);
        return ResponseEntity.ok(ApiResult.success(category));
    }
}
