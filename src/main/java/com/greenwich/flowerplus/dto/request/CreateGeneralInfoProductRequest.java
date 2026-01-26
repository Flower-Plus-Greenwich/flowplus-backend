package com.greenwich.flowerplus.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.greenwich.flowerplus.common.constant.CommonConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateGeneralInfoProductRequest(
        @Schema(description = "Tên sản phẩm", example = "Áo Thun Coolmate Basic")
        @NotBlank(message = "{validation.product.name.not_blank}")
        @Size(max = CommonConfig.PRODUCT_NAME_LENGTH, message = "{validation.product.name.size}")
        String name,

        @Schema(description = "Mô tả sản phẩm", example = "Chất liệu Cotton 100%, thoáng mát, thấm hút mồ hôi.")
        @NotBlank(message = "{validation.product.description.not_blank}")
        String description,

        @Schema(type = "string", description = "ID danh mục", example = "792254090050729589")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @NotNull(message = "{validation.product.category.required}")
        Long categoryId,

        @Schema(description = "Giá gốc", example = "150000")
        @NotNull(message = "{validation.product.base_price.required}")
        @Positive(message = "{validation.product.base_price.positive}")
        BigDecimal basePrice
) {
}
