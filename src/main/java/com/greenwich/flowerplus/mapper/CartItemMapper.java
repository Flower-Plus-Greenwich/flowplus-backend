package com.greenwich.flowerplus.mapper;

import com.greenwich.flowerplus.dto.response.CartItemResponse;
import com.greenwich.flowerplus.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    // Mapping từ Entity -> DTO
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productThumbnail", source = "product.thumbnail")
    @Mapping(target = "productSlug", source = "product.slug")
    @Mapping(target = "unitPrice", source = "product.basePrice")
    @Mapping(target = "subTotal", expression = "java(calculateSubTotal(item))")
    CartItemResponse toResponse(CartItem item);

    default BigDecimal calculateSubTotal(CartItem item) {
        if (item.getProduct() == null || item.getProduct().getBasePrice() == null) {
            return BigDecimal.ZERO;
        }
        return item.getProduct().getBasePrice().multiply(BigDecimal.valueOf(item.getQuantity()));
    }
}
