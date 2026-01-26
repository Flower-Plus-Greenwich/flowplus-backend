package com.greenwich.flowerplus.dto.request;

public record ProductRecipeRequest(
        Long materialId,
        int quantityNeeded) {
}
