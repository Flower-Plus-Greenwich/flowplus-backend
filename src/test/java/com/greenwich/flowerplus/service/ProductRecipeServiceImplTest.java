package com.greenwich.flowerplus.service;

import com.greenwich.flowerplus.common.enums.ErrorCode;
import com.greenwich.flowerplus.common.exception.AppException;
import com.greenwich.flowerplus.dto.request.ProductRecipeRequest;
import com.greenwich.flowerplus.dto.response.ProductRecipeResponse;
import com.greenwich.flowerplus.entity.Material;
import com.greenwich.flowerplus.entity.Product;
import com.greenwich.flowerplus.entity.ProductRecipe;
import com.greenwich.flowerplus.repository.MaterialRepository;
import com.greenwich.flowerplus.repository.ProductRepository;
import com.greenwich.flowerplus.service.impl.ProductRecipeServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductRecipeServiceImpl.
 * Tests business logic for recipe management including merge logic and cost calculation.
 * Uses Mockito for mocking repositories.
 */
@ExtendWith(MockitoExtension.class)
class ProductRecipeServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MaterialRepository materialRepository;

    @InjectMocks
    private ProductRecipeServiceImpl recipeService;

    // ============================================================================
    // Helper method for asserting AppException with ErrorCode
    // ============================================================================
    
    private void assertAppExceptionWithErrorCode(Runnable action, ErrorCode expectedErrorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(expectedErrorCode.getCode());
                });
    }

    // ============================================================================
    // Helper methods for creating test data
    // ============================================================================

    private Material createMaterial(Long id, String name, BigDecimal costPrice) {
        Material material = Material.builder()
                .name(name)
                .costPrice(costPrice)
                .unit("stem")
                .build();
        material.setId(id);
        return material;
    }

    private ProductRecipe createRecipe(Product product, Material material, int quantity) {
        return ProductRecipe.builder()
                .product(product)
                .material(material)
                .quantityNeeded(quantity)
                .build();
    }

    // ============================================================================
    // UPDATE PRODUCT RECIPES - MERGE LOGIC TESTS
    // ============================================================================

    @Nested
    @DisplayName("Update Product Recipes - Merge Logic Tests")
    class UpdateProductRecipesMergeLogicTests {

        @Test
        @DisplayName("Should correctly merge recipes: update existing, add new, remove missing")
        void updateProductRecipes_MergeLogic_Success() {
            // Arrange
            Long productId = 1L;
            
            // Create materials with different cost prices
            Material mat1 = createMaterial(101L, "Red Rose", new BigDecimal("1000"));
            Material mat2 = createMaterial(102L, "White Lily", new BigDecimal("2000"));
            Material mat3 = createMaterial(103L, "Sunflower", new BigDecimal("3000"));

            // Existing recipes: mat1 (qty 5) and mat2 (qty 2)
            Product product = Product.builder()
                    .productRecipes(new ArrayList<>())
                    .build();
            product.setId(productId);
            
            ProductRecipe recipe1 = createRecipe(product, mat1, 5);
            ProductRecipe recipe2 = createRecipe(product, mat2, 2);
            product.getProductRecipes().add(recipe1);
            product.getProductRecipes().add(recipe2);

            // Requests: 
            // - Update mat1 (from qty 5 to qty 10) -> UPDATE
            // - mat2 is NOT in request -> DELETE
            // - Add mat3 (qty 3) -> INSERT
            List<ProductRecipeRequest> requests = List.of(
                    new ProductRecipeRequest(101L, 10),  // Update existing
                    new ProductRecipeRequest(103L, 3)    // Add new
            );

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(materialRepository.findById(103L)).thenReturn(Optional.of(mat3));

            // Act
            recipeService.updateProductRecipes(productId, requests);

            // Assert - Should have exactly 2 recipes (mat1 updated, mat2 removed, mat3 added)
            assertThat(product.getProductRecipes()).hasSize(2);
            
            // Verify mat1 was updated to qty 10
            ProductRecipe updatedRecipe1 = product.getProductRecipes().stream()
                    .filter(r -> r.getMaterial().getId().equals(101L))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Recipe for mat1 should exist"));
            assertThat(updatedRecipe1.getQuantityNeeded()).isEqualTo(10);

            // Verify mat3 was added with qty 3
            ProductRecipe newRecipe3 = product.getProductRecipes().stream()
                    .filter(r -> r.getMaterial().getId().equals(103L))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Recipe for mat3 should exist"));
            assertThat(newRecipe3.getQuantityNeeded()).isEqualTo(3);

            // Verify mat2 was removed
            boolean hasMat2 = product.getProductRecipes().stream()
                    .anyMatch(r -> r.getMaterial().getId().equals(102L));
            assertThat(hasMat2).isFalse();

            verify(productRepository).save(product);
        }

        @Test
        @DisplayName("Should update existing recipe quantity when material exists")
        void updateProductRecipes_UpdateExistingQuantity_Success() {
            // Arrange
            Long productId = 1L;
            Material mat1 = createMaterial(101L, "Red Rose", new BigDecimal("1000"));
            
            Product product = Product.builder()
                    .productRecipes(new ArrayList<>())
                    .build();
            product.setId(productId);
            
            ProductRecipe existingRecipe = createRecipe(product, mat1, 5);
            product.getProductRecipes().add(existingRecipe);

            // Update quantity from 5 to 15
            List<ProductRecipeRequest> requests = List.of(
                    new ProductRecipeRequest(101L, 15)
            );

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            // Act
            recipeService.updateProductRecipes(productId, requests);

            // Assert
            assertThat(product.getProductRecipes()).hasSize(1);
            assertThat(product.getProductRecipes().get(0).getQuantityNeeded()).isEqualTo(15);
        }

        @Test
        @DisplayName("Should not query materialRepository for existing materials")
        void updateProductRecipes_ExistingMaterial_NoMaterialQuery() {
            // Arrange
            Long productId = 1L;
            Material mat1 = createMaterial(101L, "Red Rose", new BigDecimal("1000"));
            
            Product product = Product.builder()
                    .productRecipes(new ArrayList<>())
                    .build();
            product.setId(productId);
            
            ProductRecipe existingRecipe = createRecipe(product, mat1, 5);
            product.getProductRecipes().add(existingRecipe);

            List<ProductRecipeRequest> requests = List.of(
                    new ProductRecipeRequest(101L, 10)
            );

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            // Act
            recipeService.updateProductRecipes(productId, requests);

            // Assert - materialRepository.findById should NOT be called for existing materials
            verify(materialRepository, never()).findById(101L);
        }

        @Test
        @DisplayName("Should add new recipe when material not in existing recipes")
        void updateProductRecipes_AddNewRecipe_Success() {
            // Arrange
            Long productId = 1L;
            Material newMat = createMaterial(102L, "White Lily", new BigDecimal("2000"));
            
            Product product = Product.builder()
                    .productRecipes(new ArrayList<>())
                    .build();
            product.setId(productId);

            List<ProductRecipeRequest> requests = List.of(
                    new ProductRecipeRequest(102L, 3)
            );

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(materialRepository.findById(102L)).thenReturn(Optional.of(newMat));

            // Act
            recipeService.updateProductRecipes(productId, requests);

            // Assert
            assertThat(product.getProductRecipes()).hasSize(1);
            ProductRecipe addedRecipe = product.getProductRecipes().get(0);
            assertThat(addedRecipe.getMaterial().getId()).isEqualTo(102L);
            assertThat(addedRecipe.getQuantityNeeded()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should remove all recipes when empty request list")
        void updateProductRecipes_EmptyRequest_RemovesAllRecipes() {
            // Arrange
            Long productId = 1L;
            Material mat1 = createMaterial(101L, "Red Rose", new BigDecimal("1000"));
            
            Product product = Product.builder()
                    .productRecipes(new ArrayList<>())
                    .build();
            product.setId(productId);
            
            ProductRecipe existingRecipe = createRecipe(product, mat1, 5);
            product.getProductRecipes().add(existingRecipe);

            List<ProductRecipeRequest> requests = List.of(); // Empty list

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            // Act
            recipeService.updateProductRecipes(productId, requests);

            // Assert - All recipes should be removed
            assertThat(product.getProductRecipes()).isEmpty();
            assertThat(product.getCostPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ============================================================================
    // COST CALCULATION TESTS
    // ============================================================================

    @Nested
    @DisplayName("Cost Calculation Tests")
    class CostCalculationTests {

        @Test
        @DisplayName("Should calculate cost correctly: sum of (MaterialCost * Quantity)")
        void updateProductRecipes_CostCalculation_Correct() {
            // Arrange
            Long productId = 1L;
            
            // mat1: costPrice = 1000, qty = 10 -> 10,000
            // mat3: costPrice = 3000, qty = 3  ->  9,000
            // Total: 19,000
            Material mat1 = createMaterial(101L, "Red Rose", new BigDecimal("1000"));
            Material mat2 = createMaterial(102L, "White Lily", new BigDecimal("2000"));
            Material mat3 = createMaterial(103L, "Sunflower", new BigDecimal("3000"));

            Product product = Product.builder()
                    .productRecipes(new ArrayList<>())
                    .build();
            product.setId(productId);
            
            ProductRecipe recipe1 = createRecipe(product, mat1, 5);
            ProductRecipe recipe2 = createRecipe(product, mat2, 2);
            product.getProductRecipes().add(recipe1);
            product.getProductRecipes().add(recipe2);

            List<ProductRecipeRequest> requests = List.of(
                    new ProductRecipeRequest(101L, 10),
                    new ProductRecipeRequest(103L, 3)
            );

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(materialRepository.findById(103L)).thenReturn(Optional.of(mat3));

            // Act
            recipeService.updateProductRecipes(productId, requests);

            // Assert - Cost Calculation: 10 * 1000 + 3 * 3000 = 10000 + 9000 = 19000
            assertThat(product.getCostPrice()).isEqualByComparingTo(new BigDecimal("19000"));
        }

        @Test
        @DisplayName("Should calculate zero cost when no recipes")
        void updateProductRecipes_NoRecipes_ZeroCost() {
            // Arrange
            Long productId = 1L;
            Material mat1 = createMaterial(101L, "Red Rose", new BigDecimal("1000"));
            
            Product product = Product.builder()
                    .productRecipes(new ArrayList<>())
                    .build();
            product.setId(productId);
            
            ProductRecipe existingRecipe = createRecipe(product, mat1, 5);
            product.getProductRecipes().add(existingRecipe);

            List<ProductRecipeRequest> requests = List.of(); // Remove all

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            // Act
            recipeService.updateProductRecipes(productId, requests);

            // Assert
            assertThat(product.getCostPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle null costPrice in material (treat as zero)")
        void updateProductRecipes_NullCostPrice_TreatsAsZero() {
            // Arrange
            Long productId = 1L;
            Material matWithNullCost = createMaterial(101L, "Free Material", null);
            Material matWithCost = createMaterial(102L, "Paid Material", new BigDecimal("500"));
            
            Product product = Product.builder()
                    .productRecipes(new ArrayList<>())
                    .build();
            product.setId(productId);

            List<ProductRecipeRequest> requests = List.of(
                    new ProductRecipeRequest(101L, 10), // null cost * 10 = 0
                    new ProductRecipeRequest(102L, 4)   // 500 * 4 = 2000
            );

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(materialRepository.findById(101L)).thenReturn(Optional.of(matWithNullCost));
            when(materialRepository.findById(102L)).thenReturn(Optional.of(matWithCost));

            // Act
            recipeService.updateProductRecipes(productId, requests);

            // Assert - Only the material with cost contributes: 0 + 2000 = 2000
            assertThat(product.getCostPrice()).isEqualByComparingTo(new BigDecimal("2000"));
        }

        @Test
        @DisplayName("Should calculate cost with decimal precision")
        void updateProductRecipes_DecimalCostPrice_CorrectCalculation() {
            // Arrange
            Long productId = 1L;
            Material matWithDecimalCost = createMaterial(101L, "Premium Rose", new BigDecimal("1500.50"));
            
            Product product = Product.builder()
                    .productRecipes(new ArrayList<>())
                    .build();
            product.setId(productId);

            List<ProductRecipeRequest> requests = List.of(
                    new ProductRecipeRequest(101L, 3) // 1500.50 * 3 = 4501.50
            );

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(materialRepository.findById(101L)).thenReturn(Optional.of(matWithDecimalCost));

            // Act
            recipeService.updateProductRecipes(productId, requests);

            // Assert
            assertThat(product.getCostPrice()).isEqualByComparingTo(new BigDecimal("4501.50"));
        }
    }

    // ============================================================================
    // EXCEPTION HANDLING TESTS
    // ============================================================================

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Should throw PRODUCT_NOT_FOUND when product doesn't exist")
        void updateProductRecipes_ProductNotFound_ThrowsException() {
            // Arrange
            Long productId = 999L;
            List<ProductRecipeRequest> requests = List.of(new ProductRecipeRequest(101L, 1));

            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            // Act & Assert
            assertAppExceptionWithErrorCode(
                    () -> recipeService.updateProductRecipes(productId, requests),
                    ErrorCode.PRODUCT_NOT_FOUND);

            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw MATERIAL_NOT_FOUND when new material doesn't exist")
        void updateProductRecipes_MaterialNotFound_ThrowsException() {
            // Arrange
            Long productId = 1L;
            Product product = Product.builder()
                    .productRecipes(new ArrayList<>())
                    .build();
            product.setId(productId);

            List<ProductRecipeRequest> requests = List.of(
                    new ProductRecipeRequest(999L, 1) // Non-existent material
            );

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(materialRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertAppExceptionWithErrorCode(
                    () -> recipeService.updateProductRecipes(productId, requests),
                    ErrorCode.MATERIAL_NOT_FOUND);

            verify(productRepository, never()).save(any());
        }
    }

    // ============================================================================
    // GET RECIPES BY PRODUCT ID TESTS
    // ============================================================================

    @Nested
    @DisplayName("Get Recipes By Product ID Tests")
    class GetRecipesByProductIdTests {

        @Test
        @DisplayName("Should return recipe responses with calculated total cost")
        void getRecipesByProductId_Success() {
            // Arrange
            Long productId = 1L;
            Material mat1 = createMaterial(101L, "Red Rose", new BigDecimal("1000"));
            mat1.setUnit("stem");

            Product product = Product.builder()
                    .productRecipes(new ArrayList<>())
                    .build();
            product.setId(productId);
            
            ProductRecipe recipe = createRecipe(product, mat1, 5);
            product.getProductRecipes().add(recipe);

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            // Act
            List<ProductRecipeResponse> result = recipeService.getRecipesByProductId(productId);

            // Assert
            assertThat(result).hasSize(1);
            ProductRecipeResponse response = result.get(0);
            assertThat(response.materialId()).isEqualTo(101L);
            assertThat(response.materialName()).isEqualTo("Red Rose");
            assertThat(response.unit()).isEqualTo("stem");
            assertThat(response.costPrice()).isEqualByComparingTo(new BigDecimal("1000"));
            assertThat(response.quantityNeeded()).isEqualTo(5);
            assertThat(response.totalCost()).isEqualByComparingTo(new BigDecimal("5000")); // 1000 * 5
        }

        @Test
        @DisplayName("Should return empty list when product has no recipes")
        void getRecipesByProductId_NoRecipes_ReturnsEmptyList() {
            // Arrange
            Long productId = 1L;
            Product product = Product.builder()
                    .productRecipes(new ArrayList<>())
                    .build();
            product.setId(productId);

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            // Act
            List<ProductRecipeResponse> result = recipeService.getRecipesByProductId(productId);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should throw PRODUCT_NOT_FOUND when getting recipes for non-existent product")
        void getRecipesByProductId_ProductNotFound_ThrowsException() {
            // Arrange
            Long productId = 999L;
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            // Act & Assert
            assertAppExceptionWithErrorCode(
                    () -> recipeService.getRecipesByProductId(productId),
                    ErrorCode.PRODUCT_NOT_FOUND);
        }

        @Test
        @DisplayName("Should handle null costPrice in material when getting recipes")
        void getRecipesByProductId_NullCostPrice_ReturnsZero() {
            // Arrange
            Long productId = 1L;
            Material matWithNullCost = createMaterial(101L, "Free Material", null);

            Product product = Product.builder()
                    .productRecipes(new ArrayList<>())
                    .build();
            product.setId(productId);
            
            ProductRecipe recipe = createRecipe(product, matWithNullCost, 5);
            product.getProductRecipes().add(recipe);

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            // Act
            List<ProductRecipeResponse> result = recipeService.getRecipesByProductId(productId);

            // Assert
            assertThat(result).hasSize(1);
            ProductRecipeResponse response = result.get(0);
            assertThat(response.costPrice()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.totalCost()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }
}
