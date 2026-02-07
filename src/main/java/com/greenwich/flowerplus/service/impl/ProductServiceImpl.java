package com.greenwich.flowerplus.service.impl;

import com.greenwich.flowerplus.common.enums.ErrorCode;
import com.greenwich.flowerplus.common.enums.ProductStatus;
import com.greenwich.flowerplus.common.exception.AppException;
import com.greenwich.flowerplus.common.exception.DomainException;
import com.greenwich.flowerplus.common.utils.SlugUtils;
import com.greenwich.flowerplus.common.utils.TextValidationUtils;
import com.greenwich.flowerplus.dto.request.*;
import com.greenwich.flowerplus.dto.response.ProductResponse;
import com.greenwich.flowerplus.dto.response.ProductResponseAdmin;
import com.greenwich.flowerplus.entity.Category;
import com.greenwich.flowerplus.entity.Product;
import com.greenwich.flowerplus.entity.ProductAsset;
import com.greenwich.flowerplus.entity.ProductCategory;
import com.greenwich.flowerplus.entity.ShippingInfo;
import com.greenwich.flowerplus.infrastructure.storage.FileStorageService;
import com.greenwich.flowerplus.mapper.ProductMapper;
import com.greenwich.flowerplus.repository.CategoryRepository;
import com.greenwich.flowerplus.repository.ProductRepository;
import com.greenwich.flowerplus.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ProductServiceImpl - Single Responsibility: Product CRUD Operations
 * <p>
 * Handles all product management operations:
 * - Create, Update, Delete products
 * - Manage product assets
 * - Manage product categories
 * - Get single product by ID
 * <p>
 * NOTE: Search/listing operations are in ProductSearchServiceImpl (SRP)
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final FileStorageService fileStorageService;

    // ============================================================================
    // RETRIEVE SINGLE PRODUCT
    // ============================================================================

    @Override
    @Transactional(readOnly = true)
    public ProductResponseAdmin getProductById(Long id) {
        log.debug("Getting product by id: {} (admin)", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        return productMapper.toAdminDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductForPublic(Long id) {
        log.debug("Getting product by id: {} (public)", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        // Only return ACTIVE products for public view
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        return productMapper.toProductResponse(product);
    }

    // ============================================================================
    // CREATE
    // ============================================================================

    @Override
    @Transactional
    public ProductResponseAdmin createGeneralInfoProduct(CreateGeneralInfoProductRequest request) {
        log.info("Creating draft product: {}", request.name());

        // 0. Validate input
        validateProductName(request.name());
        validateProductDescription(request.description());
        validateProductPrice(request.basePrice(), null);

        // 1. Verify Category
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        if (!Boolean.TRUE.equals(category.getIsActive())) {
            throw new AppException(ErrorCode.CATEGORY_INACTIVE);
        }

        // 2. Generate and Verify Slug
        String slug = SlugUtils.toSlug(request.name());
        if (productRepository.existsBySlug(slug)) {
            throw new AppException(ErrorCode.SLUG_EXISTED, "Slug '" + slug + "' already exists");
        }

        ShippingInfo shippingInfo = new ShippingInfo(0, 0, 0, 0);

        // 3. Create Product Entity (DRAFT)
        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .careInstruction(null)
                .thumbnail(null)
                .slug(slug)
                .preparedQuantity(0)
                .isMakeToOrder(false)

                .basePrice(request.basePrice())
                .costPrice(BigDecimal.ZERO)
                .originalPrice(request.basePrice())

                .status(ProductStatus.DRAFT) // Force DRAFT per requirement
                .arrangementStyle(null)

                .productCategories(new ArrayList<>())
                // Default values
                .assets(new ArrayList<>())
                .productRecipes(new ArrayList<>())
                .averageRating(0.0)
                .reviewCount(0)

                .shippingInfo(shippingInfo)
                .build();

        // 4. Assign Category
        product.addCategory(category);

        // 5. Save and Flush to ensure audit info is populated
        Product savedProduct = productRepository.saveAndFlush(product);

        return productMapper.toAdminDto(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponseAdmin updateProduct(Long id, UpdateProductInfoRequest request) {
        log.info("Updating product id: {}", id);

        // Validate input
        validateProductName(request.name());
        validateProductDescription(request.description());
        validateProductDescription(request.careInstruction()); // Care instruction also needs validation
        validateProductPrice(request.basePrice(), request.originalPrice());

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        updateBasicInfo(product, request);

        updateShippingInfo(product, request);

        if (request.assets() != null) {
            handleUpdateAssets(product, request.assets());
        }

        Product savedProduct = productRepository.saveAndFlush(product);
        return productMapper.toAdminDto(savedProduct);
    }

    @Override
    @Transactional
    public void removeProduct(Long id) {
        log.info("Removing product id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        productRepository.delete(product);
    }

    @Override
    @Transactional
    public ProductResponseAdmin updateProductStatus(Long id, UpdateProductStatusRequest request) {
        log.info("Updating product status: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        //validate status is correct
        switch (request.productStatus()) {
            case ACTIVE -> product.activate();     // Domain validates requirements
            case DRAFT -> product.toDraft();
            case INACTIVE -> product.deactivate();
            default -> throw new DomainException("Invalid product status");
        }

        return productMapper.toAdminDto(productRepository.saveAndFlush(product));
    }

    // ============================================================================
    // UPDATE HELPERS
    // ============================================================================

    private void updateBasicInfo(Product product, UpdateProductInfoRequest request) {
        if (StringUtils.hasText(request.name())) {
            product.setName(request.name());
        }
        if (StringUtils.hasText(request.description())) {
            product.setDescription(request.description());
        }
        if (StringUtils.hasText(request.careInstruction())) {
            product.setCareInstruction(request.careInstruction());
        }
        if (request.basePrice() != null) {
            product.setBasePrice(request.basePrice());
        }
        if (request.originalPrice() != null) {
            product.setOriginalPrice(request.originalPrice());
        }
        if (request.isMakeToOrder() != null) {
            product.setMakeToOrder(request.isMakeToOrder());
        }
    }

    private void updateShippingInfo(Product product, UpdateProductInfoRequest request) {
        // Check if any shipping info field is present
        if (request.weight() == null && request.length() == null && 
            request.width() == null && request.height() == null) {
            return;
        }

        if (product.getShippingInfo() == null) {
            product.setShippingInfo(new ShippingInfo());
        }
        
        ShippingInfo shipping = product.getShippingInfo();
        if (request.weight() != null) shipping.setWeightInGram(request.weight());
        if (request.length() != null) shipping.setLength(request.length());
        if (request.width() != null) shipping.setWidth(request.width());
        if (request.height() != null) shipping.setHeight(request.height());
    }

    private void handleUpdateAssets(Product product, List<AssetRequest> assetRequests) {
        // Collect new URLs to confirm with Cloudinary (remove "temporary" tag)
        List<String> newUrlsToConfirm = new ArrayList<>();

        // Clear existing assets (orphanRemoval will delete them)
        // Or specific logic to merge? Usually full replace is easier for order preservation.
        product.getAssets().clear();

        for (AssetRequest assetReq : assetRequests) {
            ProductAsset asset = new ProductAsset();
            asset.setProduct(product);
            asset.setUrl(assetReq.url());
            asset.setPublicId(assetReq.publicId());
            asset.setType(assetReq.type());
            asset.setIsThumbnail(assetReq.isThumbnail() != null && assetReq.isThumbnail());
            asset.setPosition(assetReq.position() != null ? assetReq.position() : 0);
            asset.setMetaData(assetReq.metaData());

            product.getAssets().add(asset);

            // If it's a new upload (logic: maybe no ID in request, or assume all in list valid)
            // But confirming all is safe as removeTag is idempotent usually.
            // Better to only confirm check if it looks like a Cloudinary URL or just confirm all.
            newUrlsToConfirm.add(assetReq.url());
        }

        // Sync thumbnail string in Product entity
        String thumbnail = product.getAssets().stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsThumbnail()))
                .findFirst()
                .map(ProductAsset::getUrl)
                .orElse(product.getAssets().isEmpty() ? null : product.getAssets().getFirst().getUrl());
        
        product.setThumbnail(thumbnail);

        // Confirm files asynchronously/storage service
        if (!newUrlsToConfirm.isEmpty()) {
            fileStorageService.confirmFiles(newUrlsToConfirm);
        }
    }

    // ============================================================================
    // MANAGE PRODUCT ASSETS (Single Responsibility)
    // ============================================================================

    @Override
    @Transactional
    public ProductResponseAdmin manageProductAssets(Long productId, ProductAssetRequest request) {
        log.info("Managing assets for product id: {}, operation: {}", productId, request.operation());

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        switch (request.operation()) {
            case ADD -> addAssets(product, request.assets());
            case REMOVE -> removeAssets(product, request.assets());
            case REPLACE -> replaceAssets(product, request.assets());
            case SET_THUMBNAIL -> setThumbnail(product, request.thumbnailAssetId());
            case REORDER -> reorderAssets(product, request.assets());
        }

        Product savedProduct = productRepository.save(product);
        return productMapper.toAdminDto(savedProduct);
    }

    private void addAssets(Product product, List<ProductAssetRequest.AssetItem> assetItems) {
        if (assetItems == null || assetItems.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Assets list cannot be empty for ADD operation");
        }

        // Check limit (max 10 assets)
        int currentCount = product.getAssets().size();
        if (currentCount + assetItems.size() > 10) {
            throw new AppException(ErrorCode.PRODUCT_ASSET_LIMIT_EXCEEDED);
        }

        // Check for duplicate URLs
        Set<String> existingUrls = product.getAssets().stream()
                .map(ProductAsset::getUrl)
                .collect(Collectors.toSet());

        List<String> urlsToConfirm = new ArrayList<>();

        for (ProductAssetRequest.AssetItem item : assetItems) {
            validateAssetItem(item);

            if (existingUrls.contains(item.url())) {
                throw new AppException(ErrorCode.PRODUCT_ASSET_DUPLICATE_URL);
            }

            ProductAsset asset = createAssetFromItem(product, item);
            product.getAssets().add(asset);
            urlsToConfirm.add(item.url());
        }

        syncThumbnail(product);

        fileStorageService.confirmFiles(urlsToConfirm);
    }

    private void removeAssets(Product product, List<ProductAssetRequest.AssetItem> assetItems) {
        if (assetItems == null || assetItems.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Assets list cannot be empty for REMOVE operation");
        }

        Set<Long> idsToRemove = assetItems.stream()
                .map(ProductAssetRequest.AssetItem::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (idsToRemove.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Asset IDs are required for REMOVE operation");
        }

        product.getAssets().removeIf(asset -> idsToRemove.contains(asset.getId()));

        syncThumbnail(product);
    }

    private void replaceAssets(Product product, List<ProductAssetRequest.AssetItem> assetItems) {
        if (assetItems == null) {
            assetItems = new ArrayList<>();
        }

        if (assetItems.size() > 10) {
            throw new AppException(ErrorCode.PRODUCT_ASSET_LIMIT_EXCEEDED);
        }

        // Clear existing assets
        product.getAssets().clear();

        List<String> urlsToConfirm = new ArrayList<>();

        for (ProductAssetRequest.AssetItem item : assetItems) {
            validateAssetItem(item);
            ProductAsset asset = createAssetFromItem(product, item);
            product.getAssets().add(asset);
            urlsToConfirm.add(item.url());
        }

        syncThumbnail(product);

        if (!urlsToConfirm.isEmpty()) {
            fileStorageService.confirmFiles(urlsToConfirm);
        }
    }

    private void setThumbnail(Product product, Long assetId) {
        if (assetId == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Asset ID is required for SET_THUMBNAIL operation");
        }

        boolean found = false;
        for (ProductAsset asset : product.getAssets()) {
            if (asset.getId().equals(assetId)) {
                asset.setIsThumbnail(true);
                found = true;
            } else {
                asset.setIsThumbnail(false);
            }
        }

        if (!found) {
            throw new AppException(ErrorCode.PRODUCT_ASSET_NOT_FOUND);
        }

        syncThumbnail(product);
    }

    private void reorderAssets(Product product, List<ProductAssetRequest.AssetItem> assetItems) {
        if (assetItems == null || assetItems.isEmpty()) {
            return;
        }

        for (ProductAssetRequest.AssetItem item : assetItems) {
            if (item.id() != null && item.position() != null) {
                product.getAssets().stream()
                        .filter(a -> a.getId().equals(item.id()))
                        .findFirst()
                        .ifPresent(a -> a.setPosition(item.position()));
            }
        }
    }

    private void validateAssetItem(ProductAssetRequest.AssetItem item) {
        if (!TextValidationUtils.isValidUrl(item.url())) {
            throw new AppException(ErrorCode.PRODUCT_ASSET_INVALID_URL);
        }
    }

    private ProductAsset createAssetFromItem(Product product, ProductAssetRequest.AssetItem item) {
        ProductAsset asset = new ProductAsset();
        asset.setProduct(product);
        asset.setUrl(item.url());
        asset.setPublicId(item.publicId());
        asset.setType(item.type());
        asset.setIsThumbnail(item.isThumbnail() != null && item.isThumbnail());
        asset.setPosition(item.position() != null ? item.position() : 0);
        return asset;
    }

    private void syncThumbnail(Product product) {
        String thumbnail = product.getAssets().stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsThumbnail()))
                .findFirst()
                .map(ProductAsset::getUrl)
                .orElse(product.getAssets().isEmpty() ? null : product.getAssets().getFirst().getUrl());
        product.setThumbnail(thumbnail);
    }

    // ============================================================================
    // MANAGE PRODUCT CATEGORIES (Single Responsibility)
    // ============================================================================

    @Override
    @Transactional
    public ProductResponseAdmin manageProductCategories(Long productId, ProductCategoryRequest request) {
        log.info("Managing categories for product id: {}, operation: {}", productId, request.operation());

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        switch (request.operation()) {
            case ADD -> addCategories(product, request.categoryIds());
            case REMOVE -> removeCategories(product, request.categoryIds());
            case REPLACE -> replaceCategories(product, request.categoryIds());
            case CLEAR -> clearCategories(product);
        }

        Product savedProduct = productRepository.save(product);
        return productMapper.toAdminDto(savedProduct);
    }

    private void addCategories(Product product, List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Category IDs required");
        }

        List<Category> categories = categoryRepository.findAllById(categoryIds);
        if (categories.size() != categoryIds.size()) {
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        // Let domain handle all business rules
        for (Category category : categories) {
            try {
                product.addCategory(category);
            } catch (DomainException e) {
                throw new AppException(ErrorCode.DOMAIN_VALIDATION_ERROR, e.getMessage());
            }
        }
    }

    private void removeCategories(Product product, List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Category IDs cannot be empty for REMOVE operation");
        }

        Set<Long> idsToRemove = new HashSet<>(categoryIds);

        // Check if removing all categories
        long remainingCount = product.getProductCategories().stream()
                .filter(pc -> !idsToRemove.contains(pc.getCategory().getId()))
                .count();

        if (remainingCount == 0) {
            throw new AppException(ErrorCode.PRODUCT_MUST_HAVE_CATEGORY);
        }

        product.getProductCategories().removeIf(pc -> idsToRemove.contains(pc.getCategory().getId()));
    }

    private void replaceCategories(Product product, List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new AppException(ErrorCode.PRODUCT_MUST_HAVE_CATEGORY);
        }

        if (categoryIds.size() > 5) {
            throw new AppException(ErrorCode.PRODUCT_CATEGORY_LIMIT_EXCEEDED);
        }

        // Fetch all categories first to validate they exist
        List<Category> categories = categoryRepository.findAllById(categoryIds);
        if (categories.size() != categoryIds.size()) {
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        // Clear existing categories
        product.getProductCategories().clear();

        // Add new categories using domain method (handles active check, duplicate check)
        for (Category category : categories) {
            try {
                product.addCategory(category);
            } catch (DomainException e) {
                throw new AppException(ErrorCode.DOMAIN_VALIDATION_ERROR, e.getMessage());
            }
        }
    }

    private void clearCategories(Product product) {
        // Cannot clear all categories - product must have at least one
        // This operation is always invalid per business rule
        throw new AppException(ErrorCode.PRODUCT_MUST_HAVE_CATEGORY);
    }

    // ============================================================================
    // VALIDATION HELPERS
    // ============================================================================

    private void validateProductName(String name) {
        if (!StringUtils.hasText(name)) {
            return; // Allow null for partial updates
        }

        if (name.isBlank() || name.contains("''") || name.contains("\"'\"")) {
            throw new AppException(ErrorCode.PRODUCT_EMPTY_CONTENT);
        }

        var result = TextValidationUtils.validateName(name);
        if (!result.isValid()) {
            if (result.errorMessage().contains("inappropriate")) {
                throw new AppException(ErrorCode.PRODUCT_NAME_CONTAINS_BAD_WORDS, result.errorMessage());
            }
            throw new AppException(ErrorCode.PRODUCT_INVALID_NAME_FORMAT, result.errorMessage());
        }
    }

    private void validateProductDescription(String description) {
        if (!StringUtils.hasText(description)) {
            return; // Allow null for partial updates
        }

        if (description.isBlank() || description.contains("''") || description.contains("\"'\"")) {
            throw new AppException(ErrorCode.PRODUCT_EMPTY_CONTENT);
        }

        var result = TextValidationUtils.validateDescription(description);
        if (!result.isValid()) {
            if (result.errorMessage().contains("inappropriate")) {
                throw new AppException(ErrorCode.PRODUCT_DESCRIPTION_CONTAINS_BAD_WORDS, result.errorMessage());
            }
            throw new AppException(ErrorCode.PRODUCT_INVALID_DESCRIPTION_FORMAT, result.errorMessage());
        }
    }

    private static final BigDecimal MAX_PRICE = new BigDecimal("999999999999.99");

    private void validateProductPrice(BigDecimal basePrice, BigDecimal originalPrice) {
        if (basePrice != null) {
            if (basePrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new AppException(ErrorCode.PRODUCT_INVALID_PRICE);
            }
            if (basePrice.compareTo(MAX_PRICE) > 0) {
                throw new AppException(ErrorCode.INVALID_PRICE); // Using existing general price error
            }
        }

        if (originalPrice != null) {
            if (originalPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new AppException(ErrorCode.PRODUCT_INVALID_PRICE);
            }
            if (originalPrice.compareTo(MAX_PRICE) > 0) {
                throw new AppException(ErrorCode.INVALID_PRICE);
            }
        }

        if (originalPrice != null
                && basePrice != null
                && originalPrice.compareTo(basePrice) < 0) {
            throw new AppException(ErrorCode.PRODUCT_ORIGINAL_PRICE_LESS_THAN_BASE);
        }
    }
}
