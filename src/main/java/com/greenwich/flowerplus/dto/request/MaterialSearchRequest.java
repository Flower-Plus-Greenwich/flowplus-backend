package com.greenwich.flowerplus.dto.request;

import com.greenwich.flowerplus.common.enums.MaterialType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaterialSearchRequest {
    @Schema(description = "Keyword to search in name", example = "Rose")
    private String keyword;

    @Schema(description = "Filter by material type")
    private MaterialType type;

    @Schema(description = "Page number (1-indexed)", example = "1")
    private int page = 1;

    @Schema(description = "Page size", example = "20")
    private int size = 20;
}
