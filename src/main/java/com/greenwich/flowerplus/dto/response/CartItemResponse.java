package com.greenwich.flowerplus.dto.response;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public class CartItemResponse {
    private Long id;

    @NotBlank(message = "Product name cannot be blank")
    private String productName;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @Min(value = 0, message = "Unit price must be at least 0")
    private BigDecimal unitPrice;

    @Min(value = 0, message = "Subtotal must be at least 0")
    private BigDecimal subToal;

}
