package com.greenwich.flowerplus.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.greenwich.flowerplus.common.enums.ProductStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record ProductResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long id,
        String name,
        String slug,
        String description,
        BigDecimal basePrice,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long categoryId,
        String categoryName,
        ProductStatus status,
        List<AssetResponse> assets,
        String thumbnail) {
}
