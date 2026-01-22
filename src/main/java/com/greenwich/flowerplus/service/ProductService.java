package com.greenwich.flowerplus.service;

import com.greenwich.flowerplus.common.filter.ProductFilter;
import com.greenwich.flowerplus.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResponse retrieveProductById(Long id);
    Page<ProductResponse> retrieveProducts(ProductFilter filter, Pageable pageable);
}
