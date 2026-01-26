package com.greenwich.flowerplus.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ProductCategoryUpdateRequest(
        @Schema(description = "ID danh mục", example = "792254090444995412")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        List<Long> categoryIds
) {
}
