package com.greenwich.flowerplus.service;

import com.greenwich.flowerplus.common.enums.AssetType;
import com.greenwich.flowerplus.common.enums.ErrorCode;
import com.greenwich.flowerplus.common.enums.ProductStatus;
import com.greenwich.flowerplus.common.exception.AppException;
import com.greenwich.flowerplus.dto.request.*;
import com.greenwich.flowerplus.dto.response.ProductResponseAdmin;
import com.greenwich.flowerplus.entity.Category;
import com.greenwich.flowerplus.entity.Product;
import com.greenwich.flowerplus.entity.ProductAsset;
import com.greenwich.flowerplus.entity.ProductCategory;
import com.greenwich.flowerplus.infrastructure.storage.FileStorageService;
import com.greenwich.flowerplus.mapper.ProductMapper;
import com.greenwich.flowerplus.repository.CategoryRepository;
import com.greenwich.flowerplus.repository.ProductRepository;
import com.greenwich.flowerplus.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductServiceImpl.
 * Tests business logic, validation, and exception handling.
 * Uses Mockito for mocking repositories and mappers.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category activeCategory;
    private Category inactiveCategory;

    @BeforeEach
    void setUp() {
        activeCategory = Category.builder()
                .isActive(true)
                .build();
        activeCategory.setId(1L);

        inactiveCategory = Category.builder()
                .isActive(false)
                .build();
        inactiveCategory.setId(2L);
    }

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
    // CREATE PRODUCT TESTS
    // ============================================================================

    @Nested
    @DisplayName("Create General Info Product Tests")
    class CreateGeneralInfoProductTests {

        @Test
        @DisplayName("Should create product with default DRAFT status successfully")
        void createGeneralInfoProduct_Success_WithDefaultDraftStatus() {
            // Arrange
            CreateGeneralInfoProductRequest request = new CreateGeneralInfoProductRequest(
                    "Fresh Rose Bouquet", "Premium quality roses", 1L, new BigDecimal("100000"));

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(activeCategory));
            when(productRepository.existsBySlug(anyString())).thenReturn(false);
            when(productRepository.saveAndFlush(any(Product.class))).thenAnswer(i -> i.getArguments()[0]);

            ProductResponseAdmin expectedResponse = ProductResponseAdmin.builder().build();
            when(productMapper.toAdminDto(any(Product.class))).thenReturn(expectedResponse);

            // Act
            ProductResponseAdmin result = productService.createGeneralInfoProduct(request);

            // Assert
            assertThat(result).isEqualTo(expectedResponse);

            ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
            verify(productRepository).saveAndFlush(productCaptor.capture());

            Product savedProduct = productCaptor.getValue();
            assertThat(savedProduct.getName()).isEqualTo("Fresh Rose Bouquet");
            assertThat(savedProduct.getDescription()).isEqualTo("Premium quality roses");
            assertThat(savedProduct.getStatus()).isEqualTo(ProductStatus.DRAFT);
            assertThat(savedProduct.getBasePrice()).isEqualByComparingTo(new BigDecimal("100000"));
            assertThat(savedProduct.getOriginalPrice()).isEqualByComparingTo(new BigDecimal("100000"));
            assertThat(savedProduct.getCostPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        //     assertThat(savedProduct.getPreparedQuantity()).isZero();
            assertThat(savedProduct.isMakeToOrder()).isFalse();
            assertThat(savedProduct.getAverageRating()).isZero();
            assertThat(savedProduct.getReviewCount()).isZero();
        }

        @Test
        @DisplayName("Should throw exception when slug already exists")
        void createGeneralInfoProduct_DuplicateSlug_ThrowsException() {
            // Arrange
            CreateGeneralInfoProductRequest request = new CreateGeneralInfoProductRequest(
                    "Duplicate Product", "Description", 1L, new BigDecimal("100000"));

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(activeCategory));
            when(productRepository.existsBySlug(anyString())).thenReturn(true);

            // Act & Assert
            assertAppExceptionWithErrorCode(
                    () -> productService.createGeneralInfoProduct(request),
                    ErrorCode.SLUG_EXISTED);

            verify(productRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Should throw exception when product name contains bad words")
        void createGeneralInfoProduct_BadWordsInName_ThrowsException() {
            // Arrange - "fuck" is in BlockWords.badWords
            CreateGeneralInfoProductRequest request = new CreateGeneralInfoProductRequest(
                    "Fuck Rose", "Bad description", 1L, new BigDecimal("100000"));

            // Act & Assert
            assertAppExceptionWithErrorCode(
                    () -> productService.createGeneralInfoProduct(request),
                    ErrorCode.PRODUCT_NAME_CONTAINS_BAD_WORDS);

            verify(productRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void createGeneralInfoProduct_CategoryNotFound_ThrowsException() {
            // Arrange
            CreateGeneralInfoProductRequest request = new CreateGeneralInfoProductRequest(
                    "Fresh Rose", "Description", 999L, new BigDecimal("100000"));

            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertAppExceptionWithErrorCode(
                    () -> productService.createGeneralInfoProduct(request),
                    ErrorCode.CATEGORY_NOT_FOUND);
        }

        @Test
        @DisplayName("Should throw exception when category is inactive")
        void createGeneralInfoProduct_CategoryInactive_ThrowsException() {
            // Arrange
            CreateGeneralInfoProductRequest request = new CreateGeneralInfoProductRequest(
                    "Fresh Rose", "Description", 2L, new BigDecimal("100000"));

            when(categoryRepository.findById(2L)).thenReturn(Optional.of(inactiveCategory));

            // Act & Assert
            assertAppExceptionWithErrorCode(
                    () -> productService.createGeneralInfoProduct(request),
                    ErrorCode.CATEGORY_INACTIVE);
        }
    }

    // ============================================================================
    // UPDATE PRODUCT TESTS
    // ============================================================================

    @Nested
    @DisplayName("Update Product Tests")
    class UpdateProductTests {

        @Test
        @DisplayName("Should perform partial update - only updating name")
        void updateProduct_PartialUpdate_OnlyName_Success() {
            // Arrange
            Long productId = 1L;
            Product existingProduct = Product.builder()
                    .name("Old Name")
                    .description("Original Description")
                    .basePrice(new BigDecimal("50000"))
                    .originalPrice(new BigDecimal("60000"))
                    .build();

            UpdateProductInfoRequest request = new UpdateProductInfoRequest(
                    "New Name", null, null, null, null, null, null, null, null, null, null, null);

            when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
            when(productRepository.saveAndFlush(any(Product.class))).thenAnswer(i -> i.getArguments()[0]);
            when(productMapper.toAdminDto(any(Product.class))).thenReturn(ProductResponseAdmin.builder().build());

            // Act
            productService.updateProduct(productId, request);

            // Assert
            assertThat(existingProduct.getName()).isEqualTo("New Name");
            assertThat(existingProduct.getDescription()).isEqualTo("Original Description"); // Unchanged
            assertThat(existingProduct.getBasePrice()).isEqualByComparingTo(new BigDecimal("50000")); // Unchanged
        }

        @Test
        @DisplayName("Should perform partial update - only updating price")
        void updateProduct_PartialUpdate_OnlyPrice_Success() {
            // Arrange
            Long productId = 1L;
            Product existingProduct = Product.builder()
                    .name("Original Name")
                    .basePrice(new BigDecimal("50000"))
                    .originalPrice(new BigDecimal("60000"))
                    .build();

            UpdateProductInfoRequest request = new UpdateProductInfoRequest(
                    null, null, null, new BigDecimal("70000"), null, null, null, null, null, null, null, null);

            when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
            when(productRepository.saveAndFlush(any(Product.class))).thenAnswer(i -> i.getArguments()[0]);
            when(productMapper.toAdminDto(any(Product.class))).thenReturn(ProductResponseAdmin.builder().build());

            // Act
            productService.updateProduct(productId, request);

            // Assert
            assertThat(existingProduct.getName()).isEqualTo("Original Name"); // Unchanged
            assertThat(existingProduct.getBasePrice()).isEqualByComparingTo(new BigDecimal("70000"));
            assertThat(existingProduct.getOriginalPrice()).isEqualByComparingTo(new BigDecimal("60000")); // Unchanged
        }

        @Test
        @DisplayName("Should throw exception when originalPrice is less than basePrice")
        void updateProduct_OriginalPriceLessThanBase_ThrowsException() {
            // Arrange
            Long productId = 1L;
            UpdateProductInfoRequest request = new UpdateProductInfoRequest(
                    "Valid Name", "Description", null, new BigDecimal("100000"),
                    new BigDecimal("50000"), null, null, null, null, null, null, null);

            // Act & Assert
            assertAppExceptionWithErrorCode(
                    () -> productService.updateProduct(productId, request),
                    ErrorCode.PRODUCT_ORIGINAL_PRICE_LESS_THAN_BASE);

            verify(productRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void updateProduct_ProductNotFound_ThrowsException() {
            // Arrange
            Long productId = 999L;
            UpdateProductInfoRequest request = new UpdateProductInfoRequest(
                    "New Name", null, null, null, null, null, null, null, null, null, null, null);

            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            // Act & Assert
            assertAppExceptionWithErrorCode(
                    () -> productService.updateProduct(productId, request),
                    ErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    // ============================================================================
    // ASSET MANAGEMENT TESTS
    // ============================================================================

    @Nested
    @DisplayName("Manage Product Assets Tests")
    class ManageProductAssetsTests {

        @Test
        @DisplayName("Should throw exception when adding assets exceeds limit of 10")
        void manageProductAssets_AddExceedLimit_ThrowsException() {
            // Arrange
            Long productId = 1L;
            Product product = Product.builder().assets(new ArrayList<>()).build();

            // Add 9 existing assets
            for (int i = 0; i < 9; i++) {
                ProductAsset asset = new ProductAsset();
                asset.setId((long) (i + 1));
                asset.setUrl("http://url" + i + ".com");
                product.getAssets().add(asset);
            }

            // Try to add 2 more (9 + 2 = 11 > 10)
            ProductAssetRequest.AssetItem item1 = new ProductAssetRequest.AssetItem(
                    null, "http://newurl1.com", "p1", AssetType.IMAGE, false, 0);
            ProductAssetRequest.AssetItem item2 = new ProductAssetRequest.AssetItem(
                    null, "http://newurl2.com", "p2", AssetType.IMAGE, false, 1);

            ProductAssetRequest request = new ProductAssetRequest(
                    ProductAssetRequest.AssetOperation.ADD, List.of(item1, item2), null);

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            // Act & Assert
            assertAppExceptionWithErrorCode(
                    () -> productService.manageProductAssets(productId, request),
                    ErrorCode.PRODUCT_ASSET_LIMIT_EXCEEDED);
        }

        @Test
        @DisplayName("Should add assets successfully when within limit")
        void manageProductAssets_AddWithinLimit_Success() {
            // Arrange
            Long productId = 1L;
            Product product = Product.builder().assets(new ArrayList<>()).build();
            product.setId(productId);

            ProductAssetRequest.AssetItem item1 = new ProductAssetRequest.AssetItem(
                    null, "http://valid-url.com/image.jpg", "p1", AssetType.IMAGE, true, 0);

            ProductAssetRequest request = new ProductAssetRequest(
                    ProductAssetRequest.AssetOperation.ADD, List.of(item1), null);

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(productMapper.toAdminDto(any(Product.class))).thenReturn(ProductResponseAdmin.builder().build());

            // Act
            productService.manageProductAssets(productId, request);

            // Assert
            assertThat(product.getAssets()).hasSize(1);
            verify(fileStorageService).confirmFiles(anyList());
        }

        @Test
        @DisplayName("Should set thumbnail correctly - only one asset has isThumbnail=true")
        void manageProductAssets_SetThumbnail_OnlyOneThumbnail() {
            // Arrange
            Long productId = 1L;

            ProductAsset asset1 = new ProductAsset();
            asset1.setId(101L);
            asset1.setUrl("url1.jpg");
            asset1.setIsThumbnail(true); // Initially this is thumbnail

            ProductAsset asset2 = new ProductAsset();
            asset2.setId(102L);
            asset2.setUrl("url2.jpg");
            asset2.setIsThumbnail(false);

            ProductAsset asset3 = new ProductAsset();
            asset3.setId(103L);
            asset3.setUrl("url3.jpg");
            asset3.setIsThumbnail(false);

            Product product = Product.builder()
                    .assets(new ArrayList<>(List.of(asset1, asset2, asset3)))
                    .build();

            // Set asset2 as new thumbnail
            ProductAssetRequest request = new ProductAssetRequest(
                    ProductAssetRequest.AssetOperation.SET_THUMBNAIL, null, 102L);

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(productMapper.toAdminDto(any(Product.class))).thenReturn(ProductResponseAdmin.builder().build());

            // Act
            productService.manageProductAssets(productId, request);

            // Assert - Only asset2 should be thumbnail now
            assertThat(asset1.getIsThumbnail()).isFalse();
            assertThat(asset2.getIsThumbnail()).isTrue();
            assertThat(asset3.getIsThumbnail()).isFalse();
            assertThat(product.getThumbnail()).isEqualTo("url2.jpg");

            // Verify only one thumbnail exists
            long thumbnailCount = product.getAssets().stream()
                    .filter(a -> Boolean.TRUE.equals(a.getIsThumbnail()))
                    .count();
            assertThat(thumbnailCount).isEqualTo(1);
        }

        @Test
        @DisplayName("Should throw exception when setting thumbnail for non-existent asset")
        void manageProductAssets_SetThumbnail_AssetNotFound_ThrowsException() {
            // Arrange
            Long productId = 1L;
            ProductAsset asset1 = new ProductAsset();
            asset1.setId(101L);
            asset1.setUrl("url1.jpg");

            Product product = Product.builder()
                    .assets(new ArrayList<>(List.of(asset1)))
                    .build();

            ProductAssetRequest request = new ProductAssetRequest(
                    ProductAssetRequest.AssetOperation.SET_THUMBNAIL, null, 999L);

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            // Act & Assert
            assertAppExceptionWithErrorCode(
                    () -> productService.manageProductAssets(productId, request),
                    ErrorCode.PRODUCT_ASSET_NOT_FOUND);
        }
    }

    // ============================================================================
    // CATEGORY MANAGEMENT TESTS
    // ============================================================================

    @Nested
    @DisplayName("Manage Product Categories Tests")
    class ManageProductCategoriesTests {

        @Test
        @DisplayName("Should throw exception when trying to remove the last category")
        void manageProductCategories_RemoveLastCategory_ThrowsException() {
            // Arrange
            Long productId = 1L;
            Category cat1 = Category.builder().isActive(true).build();
            cat1.setId(1L);

            ProductCategory pc1 = ProductCategory.builder().category(cat1).build();
            Product product = Product.builder()
                    .productCategories(new ArrayList<>(List.of(pc1)))
                    .build();

            ProductCategoryRequest request = new ProductCategoryRequest(
                    ProductCategoryRequest.CategoryOperation.REMOVE, List.of(1L));

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            // Act & Assert
            assertAppExceptionWithErrorCode(
                    () -> productService.manageProductCategories(productId, request),
                    ErrorCode.PRODUCT_MUST_HAVE_CATEGORY);
        }

        @Test
        @DisplayName("Should remove category successfully when other categories remain")
        void manageProductCategories_RemoveCategory_OthersRemain_Success() {
            // Arrange
            Long productId = 1L;

            Category cat1 = Category.builder().isActive(true).build();
            cat1.setId(1L);
            Category cat2 = Category.builder().isActive(true).build();
            cat2.setId(2L);

            ProductCategory pc1 = ProductCategory.builder().category(cat1).build();
            ProductCategory pc2 = ProductCategory.builder().category(cat2).build();

            Product product = Product.builder()
                    .productCategories(new ArrayList<>(List.of(pc1, pc2)))
                    .build();

            ProductCategoryRequest request = new ProductCategoryRequest(
                    ProductCategoryRequest.CategoryOperation.REMOVE, List.of(1L));

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(productMapper.toAdminDto(any(Product.class))).thenReturn(ProductResponseAdmin.builder().build());

            // Act
            productService.manageProductCategories(productId, request);

            // Assert
            assertThat(product.getProductCategories()).hasSize(1);
            assertThat(product.getProductCategories().get(0).getCategory().getId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Should throw exception when adding categories exceeds limit of 5")
        void manageProductCategories_AddExceedsLimit_ThrowsException() {
            // Arrange
            Long productId = 1L;

            // Product has 4 existing categories
            List<ProductCategory> existingCategories = new ArrayList<>();
            for (int i = 1; i <= 4; i++) {
                Category cat = Category.builder().isActive(true).build();
                cat.setId((long) i);
                ProductCategory pc = ProductCategory.builder().category(cat).build();
                existingCategories.add(pc);
            }

            Product product = Product.builder()
                    .productCategories(existingCategories)
                    .build();

            // Try to add 2 more (4 + 2 = 6 > 5)
            ProductCategoryRequest request = new ProductCategoryRequest(
                    ProductCategoryRequest.CategoryOperation.ADD, List.of(5L, 6L));

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            // Note: categoryRepository stubs not needed - exception thrown before they're used

            // Act & Assert
            assertAppExceptionWithErrorCode(
                    () -> productService.manageProductCategories(productId, request),
                    ErrorCode.PRODUCT_CATEGORY_LIMIT_EXCEEDED);
        }

        @Test
        @DisplayName("Should replace categories with empty list - throws exception (must have at least one)")
        void manageProductCategories_ReplaceWithEmpty_ThrowsException() {
            // Arrange
            Long productId = 1L;
            Category cat1 = Category.builder().isActive(true).build();
            cat1.setId(1L);
            ProductCategory pc1 = ProductCategory.builder().category(cat1).build();

            Product product = Product.builder()
                    .productCategories(new ArrayList<>(List.of(pc1)))
                    .build();

            ProductCategoryRequest request = new ProductCategoryRequest(
                    ProductCategoryRequest.CategoryOperation.REPLACE, List.of());

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            // Act & Assert
            assertAppExceptionWithErrorCode(
                    () -> productService.manageProductCategories(productId, request),
                    ErrorCode.PRODUCT_MUST_HAVE_CATEGORY);
        }
    }

    // ============================================================================
    // GET PRODUCT TESTS
    // ============================================================================

    @Nested
    @DisplayName("Get Product Tests")
    class GetProductTests {

        @Test
        @DisplayName("Should get product by ID for admin successfully")
        void getProductById_Success() {
            // Arrange
            Long productId = 1L;
            Product product = Product.builder().name("Test Product").build();
            ProductResponseAdmin expectedResponse = ProductResponseAdmin.builder().build();

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(productMapper.toAdminDto(product)).thenReturn(expectedResponse);

            // Act
            ProductResponseAdmin result = productService.getProductById(productId);

            // Assert
            assertThat(result).isEqualTo(expectedResponse);
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void getProductById_NotFound_ThrowsException() {
            // Arrange
            Long productId = 999L;
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            // Act & Assert
            assertAppExceptionWithErrorCode(
                    () -> productService.getProductById(productId),
                    ErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    // ============================================================================
    // DELETE PRODUCT TESTS
    // ============================================================================

    @Nested
    @DisplayName("Remove Product Tests")
    class RemoveProductTests {

        @Test
        @DisplayName("Should remove product successfully")
        void removeProduct_Success() {
            // Arrange
            Long productId = 1L;
            Product product = Product.builder().name("Test Product").build();

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            // Act
            productService.removeProduct(productId);

            // Assert
            verify(productRepository).delete(product);
        }

        @Test
        @DisplayName("Should throw exception when product not found for removal")
        void removeProduct_NotFound_ThrowsException() {
            // Arrange
            Long productId = 999L;
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            // Act & Assert
            assertAppExceptionWithErrorCode(
                    () -> productService.removeProduct(productId),
                    ErrorCode.PRODUCT_NOT_FOUND);

            verify(productRepository, never()).delete(any(Product.class));
        }
    }
}
