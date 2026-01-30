package com.greenwich.flowerplus.controller;

import com.greenwich.flowerplus.common.ApiResult;
import com.greenwich.flowerplus.dto.request.MaterialRequest;
import com.greenwich.flowerplus.dto.request.MaterialSearchRequest;
import com.greenwich.flowerplus.dto.response.MaterialResponse;
import com.greenwich.flowerplus.service.MaterialService;
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
@RequestMapping("/api/v1/admin/materials")
@Tag(name = "Material Management", description = "APIs for managing raw materials (Master Data)")
public class MaterialController {

    private final MaterialService materialService;

    @Operation(summary = "Search materials with pagination and filtering")
    @PreAuthorize("hasAnyAuthority('ROLE_SHOP_OWNER', 'ROLE_SHOP_STAFF', 'ROLE_ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<ApiResult<List<MaterialResponse>>> searchMaterials(
            @ModelAttribute MaterialSearchRequest request) {
        log.info("Searching materials: keyword={}, type={}, page={}, size={}", 
                request.getKeyword(), request.getType(), request.getPage(), request.getSize());
        
        Page<MaterialResponse> result = materialService.searchMaterials(request);
        return ResponseEntity.ok(ApiResult.success(
                result.getContent(),
                request.getPage(),
                request.getSize(),
                result.getTotalElements()
        ));
    }

    @Operation(summary = "Get all materials (unpaged, sorted by name)")
    @PreAuthorize("hasAnyAuthority('ROLE_SHOP_OWNER', 'ROLE_SHOP_STAFF', 'ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResult<List<MaterialResponse>>> getMaterials() {
        return ResponseEntity.ok(ApiResult.success(materialService.getAllMaterials()));
    }

    @Operation(summary = "Get material by ID")
    @PreAuthorize("hasAnyAuthority('ROLE_SHOP_OWNER', 'ROLE_SHOP_STAFF', 'ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<MaterialResponse>> getMaterialById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResult.success(materialService.getMaterialById(id)));
    }

    @Operation(summary = "Create a new material")
    @PreAuthorize("hasAnyAuthority('ROLE_SHOP_OWNER', 'ROLE_SHOP_STAFF', 'ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResult<MaterialResponse>> createMaterial(
            @Valid @RequestBody MaterialRequest request) {
        MaterialResponse response = materialService.createMaterial(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success(response, "Material created successfully"));
    }

    @Operation(summary = "Update an existing material")
    @PreAuthorize("hasAnyAuthority('ROLE_SHOP_OWNER', 'ROLE_SHOP_STAFF', 'ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResult<MaterialResponse>> updateMaterial(
            @PathVariable Long id,
            @Valid @RequestBody MaterialRequest request) {
        MaterialResponse response = materialService.updateMaterial(id, request);
        return ResponseEntity.ok(ApiResult.success(response, "Material updated successfully"));
    }

    @Operation(summary = "Delete a material (soft delete)")
    @PreAuthorize("hasAnyAuthority('ROLE_SHOP_OWNER', 'ROLE_SHOP_STAFF', 'ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResult<Void>> deleteMaterial(@PathVariable Long id) {
        materialService.deleteMaterial(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResult.success(null, "Material deleted successfully"));
    }
}

