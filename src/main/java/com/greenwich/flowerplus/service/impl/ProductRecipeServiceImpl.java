package com.greenwich.flowerplus.service.impl;

import com.greenwich.flowerplus.common.enums.ErrorCode;
import com.greenwich.flowerplus.common.exception.AppException;
import com.greenwich.flowerplus.dto.request.ProductRecipeRequest;
import com.greenwich.flowerplus.dto.response.ProductRecipeResponse;
import com.greenwich.flowerplus.entity.Material;
import com.greenwich.flowerplus.entity.Product;
import com.greenwich.flowerplus.entity.ProductRecipe;
import com.greenwich.flowerplus.repository.MaterialRepository;
import com.greenwich.flowerplus.repository.ProductRepository;
import com.greenwich.flowerplus.service.ProductRecipeService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductRecipeServiceImpl implements ProductRecipeService {

    private final ProductRepository productRepository;
    private final MaterialRepository materialRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProductRecipes(Long productId, List<ProductRecipeRequest> requests) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        // 1. Chuẩn bị Map từ Request để tra cứu nhanh (O(1))
        // Key: MaterialId, Value: QuantityNeeded
        Map<Long, Integer> requestMap = requests.stream()
                .collect(Collectors.toMap(ProductRecipeRequest::materialId, ProductRecipeRequest::quantityNeeded));

        // 2. Duyệt qua list HIỆN CÓ trong DB
        // Dùng Iterator để có thể xóa phần tử an toàn trong lúc duyệt
        Iterator<ProductRecipe> iterator = product.getProductRecipes().iterator();
        while (iterator.hasNext()) {
            ProductRecipe existingRecipe = iterator.next();
            Long materialId = existingRecipe.getMaterial().getId();

            if (requestMap.containsKey(materialId)) {
                // CASE A: Cả 2 đều có -> UPDATE số lượng
                Integer newQuantity = requestMap.get(materialId);
                // Chỉ update nếu số lượng khác nhau (đỡ tốn query update thừa)
                if (existingRecipe.getQuantityNeeded() != newQuantity) {
                    existingRecipe.setQuantityNeeded(newQuantity);
                }
                // Xóa khỏi map để đánh dấu là đã xử lý xong item này
                requestMap.remove(materialId);
            } else {
                // CASE B: DB có mà Request không có -> DELETE
                // orphanRemoval = true sẽ tự động xóa record trong DB khi remove khỏi list
                iterator.remove();
            }
        }

        // 3. Những cái còn sót lại trong Map là cái MỚI -> INSERT
        for (Map.Entry<Long, Integer> entry : requestMap.entrySet()) {
            Material material = materialRepository.findById(entry.getKey())
                    .orElseThrow(() -> new AppException(ErrorCode.MATERIAL_NOT_FOUND));

            ProductRecipe newRecipe = ProductRecipe.builder()
                    .product(product)
                    .material(material)
                    .quantityNeeded(entry.getValue())
                    .build();

            // Add vào list, JPA sẽ tự INSERT
            product.addRecipe(newRecipe);
        }

        // 4. Tính toán lại Cost Price (Logic không đổi)
        // Lưu ý: Phải tính dựa trên list `product.getProductRecipes()` hiện tại (đã merge xong)
        BigDecimal totalCost = BigDecimal.ZERO;
        for (ProductRecipe r : product.getProductRecipes()) {
            if (r.getMaterial().getCostPrice() != null) {
                BigDecimal itemCost = r.getMaterial().getCostPrice()
                        .multiply(BigDecimal.valueOf(r.getQuantityNeeded()));
                totalCost = totalCost.add(itemCost);
            }
        }

        product.setCostPrice(totalCost);
        productRepository.save(product);
    }

    @Override
    public List<ProductRecipeResponse> getRecipesByProductId(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        return product.getProductRecipes().stream()
                .map(recipe -> {
                    Material material = recipe.getMaterial();
                    BigDecimal costPrice = material.getCostPrice() != null ? material.getCostPrice() : BigDecimal.ZERO;
                    BigDecimal itemTotalCost = costPrice.multiply(BigDecimal.valueOf(recipe.getQuantityNeeded()));

                    return ProductRecipeResponse.builder()
                            .materialId(material.getId())
                            .materialName(material.getName())
                            .unit(material.getUnit())
                            .costPrice(costPrice)
                            .quantityNeeded(recipe.getQuantityNeeded())
                            .totalCost(itemTotalCost)
                            .build();
                })
                .toList();
    }
}