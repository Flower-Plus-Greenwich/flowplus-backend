package com.greenwich.flowerplus.service;

import com.greenwich.flowerplus.dto.response.CartResponse;

public interface CartService {
    CartResponse getCart(Long userId);
}
