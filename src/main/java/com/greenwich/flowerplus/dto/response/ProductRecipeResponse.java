package com.greenwich.flowerplus.dto.response;

import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record ProductRecipeResponse(
        Long materialId,
        String materialName,
        String unit,
        BigDecimal costPrice,
        int quantityNeeded,
        BigDecimal totalCost
) {
}
