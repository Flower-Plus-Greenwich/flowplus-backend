package com.greenwich.flowerplus.dto.request;

import com.greenwich.flowerplus.common.enums.OrderItemType;
import com.greenwich.flowerplus.dto.snapshot.OrderItemCustomConfig;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record OrderItemRequest(

        Long productId, // Nullable nếu là custom

        @NotNull(message = "Loại sản phẩm không được để trống")
        OrderItemType itemType,

        @Min(value = 1, message = "Số lượng phải lớn hơn 0")
        Integer quantity,

        BigDecimal unitPrice,
        BigDecimal unitCost,

        OrderItemCustomConfig customConfig

) {
    public boolean isProduct() {
        return OrderItemType.PRODUCT.equals(this.itemType);
    }
}
