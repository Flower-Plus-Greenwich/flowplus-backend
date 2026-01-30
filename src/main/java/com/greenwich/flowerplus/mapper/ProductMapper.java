package com.greenwich.flowerplus.mapper;

import com.greenwich.flowerplus.dto.response.AssetResponse;
import com.greenwich.flowerplus.dto.response.ProductListingDto;
import com.greenwich.flowerplus.dto.response.ProductResponse;
import com.greenwich.flowerplus.dto.response.ProductResponseAdmin;
import com.greenwich.flowerplus.entity.Product;
import com.greenwich.flowerplus.entity.ProductAsset;
import com.greenwich.flowerplus.entity.ProductCategory;
import com.greenwich.flowerplus.dto.snapshot.CategorySnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Collections;
import java.util.List;

/**
 * ProductMapper - Maps Product entity to various DTOs
 * 
 * Supports multiple category relationships via ProductCategory
 */
@Mapper(componentModel = "spring", 
        uses = {AuditorMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    // ============================================================================
    // CUSTOMER-FACING PRODUCT RESPONSE (Product Detail Page)
    // ============================================================================

    @Mapping(target = "primaryCategory", source = "productCategories", qualifiedByName = "mapFirstCategorySnapshot")
    @Mapping(target = "categories", source = "productCategories", qualifiedByName = "mapAllCategorySnapshots")
    @Mapping(target = "assets", source = "assets")
    @Mapping(target = "weight", source = "shippingInfo.weightInGram")
    @Mapping(target = "length", source = "shippingInfo.length")
    @Mapping(target = "width", source = "shippingInfo.width")
    @Mapping(target = "height", source = "shippingInfo.height")
    @Mapping(target = "inStock", source = "makeToOrder")
    ProductResponse toProductResponse(Product product);

    // ============================================================================
    // LIGHTWEIGHT LISTING DTO (for search results - storefront)
    // ============================================================================

    @Mapping(target = "price", source = "basePrice")
    @Mapping(target = "categoryName", source = "productCategories", qualifiedByName = "mapFirstCategoryName")
    @Mapping(target = "categories", source = "productCategories", qualifiedByName = "mapAllCategorySnapshots")
    @Mapping(target = "inStock", source = "makeToOrder")
    ProductListingDto toListingDto(Product product);

    // ============================================================================
    // ADMIN RESPONSE (Backoffice - full details)
    // ============================================================================

    @Mapping(target = "primaryCategory", source = "productCategories", qualifiedByName = "mapFirstCategorySnapshot")
    @Mapping(target = "categories", source = "productCategories", qualifiedByName = "mapAllCategorySnapshots")
    @Mapping(target = "assets", source = "assets")
    @Mapping(target = "createdBy", source = "createdBy", qualifiedByName = "mapAuditor")
    @Mapping(target = "updatedBy", source = "updatedBy", qualifiedByName = "mapAuditor")
    @Mapping(target = "weight", source = "shippingInfo.weightInGram")
    @Mapping(target = "length", source = "shippingInfo.length")
    @Mapping(target = "width", source = "shippingInfo.width")
    @Mapping(target = "height", source = "shippingInfo.height")
    @Mapping(target = "inStock", source = "makeToOrder")
    ProductResponseAdmin toAdminDto(Product product);

    // ============================================================================
    // HELPER METHODS - Category Mapping
    // ============================================================================

    @Named("mapFirstCategoryName")
    default String mapFirstCategoryName(List<ProductCategory> productCategories) {
        if (productCategories == null || productCategories.isEmpty()) {
            return null;
        }
        return productCategories.getFirst().getCategory().getName();
    }

    @Named("mapFirstCategorySnapshot")
    default CategorySnapshot mapFirstCategorySnapshot(List<ProductCategory> productCategories) {
        if (productCategories == null || productCategories.isEmpty()) {
            return null;
        }
        var category = productCategories.getFirst().getCategory();
        return new CategorySnapshot(category.getId(), category.getName());
    }

    @Named("mapAllCategorySnapshots")
    default List<CategorySnapshot> mapAllCategorySnapshots(List<ProductCategory> productCategories) {
        if (productCategories == null || productCategories.isEmpty()) {
            return Collections.emptyList();
        }
        return productCategories.stream()
                .map(pc -> new CategorySnapshot(pc.getCategory().getId(), pc.getCategory().getName()))
                .toList();
    }

    // ============================================================================
    // HELPER METHODS - Auditor Mapping
    // ============================================================================

    // Handled by AuditorMapper component via 'uses'

    // ============================================================================
    // HELPER METHODS - Asset Mapping
    // ============================================================================

    AssetResponse toAssetResponse(ProductAsset asset);
}
