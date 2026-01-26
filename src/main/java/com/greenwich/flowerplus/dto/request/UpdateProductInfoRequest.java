package com.greenwich.flowerplus.dto.request;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.greenwich.flowerplus.common.constant.CommonConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record UpdateProductInfoRequest(

        @Schema(description = "Tên sản phẩm", example = "Giỏ hoa")
        @Size(max = CommonConfig.PRODUCT_NAME_LENGTH, message = "Product name cannot exceed {max} characters")
        String name,

        @Schema(description = "Mô tả sản phẩm", example = "Tổng hợp từ những loại hoa hồng.")
        String description,

        @Schema(description = "Field này dành cho hướng dẫn chăm sóc, dùng WYSIWYG", example = "<p>hello </p>")
        String careInstruction,

        @Schema(description = "Giá gốc", example = "150000")
        @PositiveOrZero(message = "Base price must be positive or zero")
        @Digits(integer = 13, fraction = 2, message = "Base price must have at most 13 digits before and 2 digits after the decimal point")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal basePrice,

        @Schema(description = "Giá niêm yết", example = "150000")
        @PositiveOrZero(message = "Original price must be positive or zero")
        @Digits(integer = 13, fraction = 2, message = "Original price must have at most 13 digits before and 2 digits after the decimal point")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal originalPrice,

        @Schema(description = "Slug sản phẩm (URL-friendly)", example = "gio-hoa-valentine")
        String slug,

        @Valid
        @Schema(description = "Danh sách assets (ảnh/video) của sản phẩm")
        List<AssetRequest> assets,

        @Schema(description = "Cân nặng (gram)", example = "300")
        Integer weight,

        @Schema(description = "Chiều dài (cm)", example = "30")
        Integer length,

        @Schema(description = "Chiều rộng (cm)", example = "20")
        Integer width,

        @Schema(description = "Chiều cao (cm)", example = "10")
        Integer height,

        @Schema(description = "Hàng đặt làm (Pre-order)", example = "false")
        Boolean isMakeToOrder

) implements ProductRequest {
        @Override
        public List<AssetRequest> assets() {
                return assets;
        }
}
