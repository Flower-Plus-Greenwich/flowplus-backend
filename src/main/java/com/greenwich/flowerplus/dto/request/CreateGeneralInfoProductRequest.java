package com.greenwich.flowerplus.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.greenwich.flowerplus.common.constant.CommonConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;

public record CreateGeneralInfoProductRequest(
        @Schema(description = "Tên sản phẩm", example = "Áo Thun Coolmate Basic")
        @NotBlank(message = "Product name cannot be blank")
        @Size(max = CommonConfig.PRODUCT_NAME_LENGTH, message = "Product name cannot exceed {max} characters")
        String name,

        @Schema(description = "Mô tả sản phẩm", example = "Chất liệu Cotton 100%, thoáng mát, thấm hút mồ hôi.")
        @NotBlank(message = "Product description cannot be blank")
        String description,

        @Schema(type = "string", description = "ID danh mục", example = "792254090050729589")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @NotNull(message = "Category ID is required")
        Long categoryId,

        @Schema(description = "Giá gốc", example = "150000")
        @NotNull(message = "Base price is required")
        @Positive(message = "Base price must be positive")
        @Digits(integer = 13, fraction = 2, message = "Price must have at most 13 digits before and 2 digits after the decimal point")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal basePrice
) {
}
