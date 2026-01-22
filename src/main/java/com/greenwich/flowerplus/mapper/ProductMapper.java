package com.greenwich.flowerplus.mapper;

import com.greenwich.flowerplus.dto.response.AssetResponse;
import com.greenwich.flowerplus.dto.response.AuditorResponse;
import com.greenwich.flowerplus.dto.response.ProductListingDto;
import com.greenwich.flowerplus.dto.response.ProductResponse;
import com.greenwich.flowerplus.dto.response.ProductResponseAdmin;
import com.greenwich.flowerplus.entity.Category;
import com.greenwich.flowerplus.entity.Product;
import com.greenwich.flowerplus.entity.ProductAsset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "categoryId", source = "categories", qualifiedByName = "mapFirstCategoryId")
    @Mapping(target = "categoryName", source = "categories", qualifiedByName = "mapFirstCategoryName")
    @Mapping(target = "assets", source = "assets")
    ProductResponse toProductResponse(Product product);

    @Mapping(target = "thumbnail", source = "thumbnail")
    @Mapping(target = "price", source = "basePrice")
    @Mapping(target = "categoryName", source = "categories", qualifiedByName = "mapFirstCategoryName")
    @Mapping(target = "availableStock", source = "preparedQuantity")
    @Mapping(target = "inStock", expression = "java(product.getPreparedQuantity() > 0 || product.isMakeToOrder())")
    ProductListingDto toListingDto(Product product);

    @Mapping(target = "categoryName", source = "categories", qualifiedByName = "mapFirstCategoryName")
    @Mapping(target = "categoryId", source = "categories", qualifiedByName = "mapFirstCategoryId")
    @Mapping(target = "assets", source = "assets")
    @Mapping(target = "createdBy", source = "createdBy", qualifiedByName = "mapAuditorId")
    @Mapping(target = "updatedBy", source = "updatedBy", qualifiedByName = "mapAuditorId")
    ProductResponseAdmin toAdminDto(Product product);

    @Named("mapFirstCategoryName")
    default String mapFirstCategoryName(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return null;
        }
        return categories.get(0).getName();
    }

    @Named("mapFirstCategoryId")
    default Long mapFirstCategoryId(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return null;
        }
        return categories.getFirst().getId();
    }

    @Named("mapAuditorId")
    default AuditorResponse mapAuditorId(String idStr) {
        if (idStr == null) return null;
        try {
            Long id = Long.parseLong(idStr);
            return AuditorResponse.builder().id(id).build();
        } catch (NumberFormatException e) {
            return null;
        }
    }

    AssetResponse toAssetResponse(ProductAsset asset);
}
