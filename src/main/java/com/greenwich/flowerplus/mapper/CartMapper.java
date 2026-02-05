package com.greenwich.flowerplus.mapper;

import com.greenwich.flowerplus.dto.response.CartResponse;
import com.greenwich.flowerplus.entity.Cart;
import com.greenwich.flowerplus.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", uses = {CartItemMapper.class})
public interface CartMapper {

    @Mapping(target = "items", source = "items") // Tự động dùng CartItemMapper cho List này
    @Mapping(target = "totalItems", expression = "java(calculateTotalItems(cart))")
    @Mapping(target = "totalPrice", expression = "java(calculateTotalPrice(cart))")
    CartResponse toResponse(Cart cart);

    default Integer calculateTotalItems(Cart cart) {
        if (cart.getItems() == null) return 0;
        return cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    default BigDecimal calculateTotalPrice(Cart cart) {
        if (cart.getItems() == null) return BigDecimal.ZERO;
        return cart.getItems().stream()
                .map(item -> {
                    BigDecimal price = item.getProduct().getBasePrice();
                    return price.multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
