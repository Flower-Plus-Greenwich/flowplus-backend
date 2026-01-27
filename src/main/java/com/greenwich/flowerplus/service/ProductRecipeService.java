package com.greenwich.flowerplus.service;

import com.greenwich.flowerplus.dto.request.ProductRecipeRequest;
import com.greenwich.flowerplus.dto.response.ProductRecipeResponse;

import java.util.List;

public interface ProductRecipeService {

    void updateProductRecipes(Long productId, List<ProductRecipeRequest> requests);

    List<ProductRecipeResponse> getRecipesByProductId(Long productId);
}
