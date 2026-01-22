package com.greenwich.flowerplus.common.filter;

import java.math.BigDecimal;

public record ProductFilter (
        String keyword,        // Tìm theo tên
        Long categoryId,       // Lọc theo danh mục
        BigDecimal minPrice,   // Giá từ
        BigDecimal maxPrice    // Giá đến
) {
}