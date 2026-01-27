package com.greenwich.flowerplus.controller;

import com.greenwich.flowerplus.common.ApiResult;
import com.greenwich.flowerplus.dto.request.ProductRecipeRequest;
import com.greenwich.flowerplus.dto.response.ProductRecipeResponse;
import com.greenwich.flowerplus.service.ProductRecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products/{productId}/recipes")
@RequiredArgsConstructor
@Tag(name = "Product Recipe API", description = "Quản lý công thức/thành phần của sản phẩm")
public class ProductRecipeController {

    private final ProductRecipeService recipeService;

    @Operation(summary = "Lấy danh sách thành phần của bó hoa")
    @GetMapping
    public ResponseEntity<ApiResult<List<ProductRecipeResponse>>> getRecipes(
            @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResult.success(recipeService.getRecipesByProductId(productId), "Retrieved list of recipes"));
    }

    @Operation(summary = "Cập nhật toàn bộ công thức (Thay thế hoàn toàn)")
    @PutMapping
    @PreAuthorize("hasAnyAuthority('ROLE_SHOP_OWNER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResult<Void>> updateRecipes(
            @PathVariable Long productId,
            @Valid @RequestBody List<ProductRecipeRequest> requests) {

        recipeService.updateProductRecipes(productId, requests);
        return ResponseEntity.ok(ApiResult.success(null, "Update recipes successfully"));
    }
}