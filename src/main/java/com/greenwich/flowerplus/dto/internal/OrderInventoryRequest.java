package com.greenwich.flowerplus.dto.internal;

import java.util.List;

public record OrderInventoryRequest(
        String orderCode,
        List<OrderItemSnapshot> items
) {
}
