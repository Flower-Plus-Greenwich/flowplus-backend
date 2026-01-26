package com.greenwich.flowerplus.repository;

import com.greenwich.flowerplus.entity.Material;
import com.greenwich.flowerplus.entity.Product;
import com.greenwich.flowerplus.entity.ProductRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRecipeRepository extends JpaRepository<ProductRecipe, Long> {
    
    Optional<ProductRecipe> findByProductAndMaterial(Product product, Material material);

    void deleteByProductIdAndMaterialId(Long productId, Long materialId);

    List<ProductRecipe> findByProduct(Product product);
}
