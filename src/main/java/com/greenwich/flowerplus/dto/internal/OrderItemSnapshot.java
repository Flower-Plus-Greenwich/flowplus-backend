package com.greenwich.flowerplus.dto.internal;

public record OrderItemSnapshot(
    Long productId,
    int quantity
) {
}
