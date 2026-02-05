package com.greenwich.flowerplus.service;

import com.greenwich.flowerplus.dto.request.CartAddItemRequest;
import com.greenwich.flowerplus.dto.response.CartResponse;

public interface CartService {
    CartResponse getCart(Long userId, String token);
    CartResponse addToCart(Long userId, CartAddItemRequest request);
    CartResponse updateCart(Long userId, CartAddItemRequest request);
//    CartResponse UpdateCartItem(Long userId, Long productId, Integer quantity);
}
