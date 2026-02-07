package com.greenwich.flowerplus.dto.request;

import com.greenwich.flowerplus.common.enums.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {

    @Schema(description = "Category name", example = "Valentine's Day 2024")
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;

    @Schema(description = "Category description for collection context", example = "Romantic flowers for Valentine's Day")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Schema(description = "Thumbnail URL for visual representation")
    private String thumbnail;

    @Schema(description = "Category type", example = "OCCASION")
    private CategoryType type;

    @Schema(description = "Parent category ID for hierarchical structure")
    private Long parentId;

    @Schema(description = "Whether the category is active (for seasonal collections)", example = "true")
    private Boolean isActive = true;
}
