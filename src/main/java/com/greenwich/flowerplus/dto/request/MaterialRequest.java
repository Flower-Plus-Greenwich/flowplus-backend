package com.greenwich.flowerplus.dto.request;

import com.greenwich.flowerplus.common.enums.MaterialType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MaterialRequest {
    @Schema(description = "Material name", example = "Red Rose")
    @NotBlank(message = "Material name is required")
    private String name;

    @Schema(description = "Unit of measurement", example = "Stem")
    @NotBlank(message = "Unit is required")
    private String unit;

    @Schema(description = "Image URL for the material")
    private String imageUrl;

    @Schema(description = "Estimated cost price", example = "5000")
    @PositiveOrZero(message = "Cost price must be zero or positive")
    private BigDecimal costPrice;

    @Schema(description = "Estimated selling price (if sold separately)", example = "15000")
    @PositiveOrZero(message = "Selling price must be zero or positive")
    private BigDecimal sellingPrice;

    @Schema(description = "Type of material")
    @NotNull(message = "Material type is required")
    private MaterialType type;
}
