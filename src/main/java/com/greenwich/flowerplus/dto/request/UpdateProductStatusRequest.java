package com.greenwich.flowerplus.dto.request;

import com.greenwich.flowerplus.common.enums.ProductStatus;

public record UpdateProductStatusRequest(
        ProductStatus productStatus
) {
}

