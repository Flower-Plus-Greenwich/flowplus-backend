package com.greenwich.flowerplus.dto.request;

import com.greenwich.flowerplus.common.enums.ProductSort;
import com.greenwich.flowerplus.common.enums.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductSearchRequest {
    private String keyword;
    private String categoryId; // Kept as String to avoid precision issues if using TSID
    private String categorySlug;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    private ProductStatus status;

    private int page = 0;
    private int size = 20;
    private ProductSort sort = ProductSort.NEWEST;
}