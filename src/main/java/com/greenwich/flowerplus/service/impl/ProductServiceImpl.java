package com.greenwich.flowerplus.service.impl;

import com.greenwich.flowerplus.common.enums.ProductStatus;
import com.greenwich.flowerplus.common.filter.ProductFilter;
import com.greenwich.flowerplus.common.search.GenericSpecificationBuilder;
import com.greenwich.flowerplus.dto.request.ProductSearchRequest;
import com.greenwich.flowerplus.dto.response.ProductListingDto;
import com.greenwich.flowerplus.dto.response.ProductResponse;
import com.greenwich.flowerplus.dto.response.ProductResponseAdmin;
import com.greenwich.flowerplus.entity.Product;
import com.greenwich.flowerplus.mapper.ProductMapper;
import com.greenwich.flowerplus.repository.ProductRepository;
import com.greenwich.flowerplus.common.enums.ErrorCode;
import com.greenwich.flowerplus.common.exception.AppException;
import com.greenwich.flowerplus.service.ProductSearchService;
import com.greenwich.flowerplus.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Slf4j
@Service
public class ProductServiceImpl implements ProductService, ProductSearchService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductResponse retrieveProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        return productMapper.toProductResponse(product);
    }

    @Override
    public Page<ProductResponse> retrieveProducts(ProductFilter filter, Pageable pageable) {
        GenericSpecificationBuilder<Product> builder = new GenericSpecificationBuilder<>();

        if (StringUtils.hasText(filter.keyword())) {
            builder.with("name", ":", filter.keyword(), "*", "*");
        }
        if (filter.categoryId() != null) {
            builder.with("categories.id", ":", filter.categoryId(), null, null);
        }
        if (filter.minPrice() != null) {
            builder.with("basePrice", ">", filter.minPrice(), null, null);
        }
        if (filter.maxPrice() != null) {
            builder.with("basePrice", "<", filter.maxPrice(), null, null);
        }

        Specification<Product> spec = builder.build();
        if (spec == null) {
            spec = (root, query, cb) -> cb.conjunction();
        }

        return productRepository.findAll(spec, pageable)
                .map(productMapper::toProductResponse);
    }

    @Override
    public Page<ProductListingDto> searchPublic(ProductSearchRequest req) {
        // Force ACTIVE status for public/customer searches
        req.setStatus(ProductStatus.ACTIVE);

        // Build specification
        Specification<Product> spec = buildSpecification(req);

        // Build pageable with sort
        Pageable pageable = buildPageable(req);

        // Execute query and map to DTO
        return productRepository.findAll(spec, pageable)
                .map(productMapper::toListingDto);
    }

    /**
     * Builds JPA Specification using GenericSpecificationBuilder
     * Supports: keyword, categoryId, price range, status
     */
    private Specification<Product> buildSpecification(ProductSearchRequest req) {
        GenericSpecificationBuilder<Product> builder = new GenericSpecificationBuilder<>();

        // Keyword search (name contains)
        if (StringUtils.hasText(req.getKeyword())) {
            builder.with("name", ":", req.getKeyword(), "*", "*");
        }

        // Category filter (by ID)
        if (StringUtils.hasText(req.getCategoryId())) {
            try {
                // Assuming categories is a collection, we might need a join or simpler check
                // For simplicity in GenericSpecificationBuilder which usually handles direct fields:
                // If GenericSpecBuilder doesn't support joins well, we might need manual Join.
                // However, since we are simplifying:
                // Let's assume existing builder can handle "categories.id" or we implement custom logic if it fails.
                // Based on previous code "category.id" implies ManyToOne, but Entity has List<Category>.
                // We will attempt "categories.id" which is standard for Joins in Specs.
                builder.with("categories.id", ":", Long.valueOf(req.getCategoryId()), null, null);
            } catch (NumberFormatException e) {
                log.warn("Invalid categoryId format: {}", req.getCategoryId());
            }
        }

        // Category filter (by Slug)
        if (StringUtils.hasText(req.getCategorySlug())) {
            builder.with("categories.slug", ":", req.getCategorySlug(), null, null);
        }

        // Price range
        if (req.getMinPrice() != null) {
            builder.with("basePrice", ">", req.getMinPrice(), null, null);
        }

        if (req.getMaxPrice() != null) {
            builder.with("basePrice", "<", req.getMaxPrice(), null, null);
        }

        // Status filter (can be null for admin "get all")
        if (req.getStatus() != null) {
            builder.with("status", ":", req.getStatus(), null, null);
        }

        return builder.build();
    }

    @Override
    public Page<ProductResponseAdmin> searchAdmin(ProductSearchRequest req) {
        // Admin can see all statuses or filter by specific status
        // If status is null, all products are returned

        // Build specification
        Specification<Product> spec = buildSpecification(req);

        if (spec == null) {
            spec = (root, query, cb) -> cb.conjunction(); // Always true condition
        }

        // Build pageable with sort
        Pageable pageable = buildPageable(req);

        Page<Product> productPage = productRepository.findAll(spec, pageable);

        if (productPage.isEmpty()) {
            return Page.empty(pageable);
        }

        // Map to AdminDTO
        return productPage.map(productMapper::toAdminDto);
    }

    /**
     * Builds Pageable with sorting logic
     * Supports: newest, price_asc, price_desc, name_asc, name_desc
     */
    private Pageable buildPageable(ProductSearchRequest req) {
        // Normalize page number (1-based to 0-based)
        int page = req.getPage();
        if (page >= 1) {
            page = page - 1;
        }

        // Build sort
        Sort sort = buildSort(req.getSort().getValue());

        return PageRequest.of(page, req.getSize(), sort);
    }

    /**
     * Maps sort string to Sort object
     */
    private Sort buildSort(String sortParam) {
        return switch (sortParam != null ? sortParam : "newest") {
            case "price_asc" -> Sort.by("basePrice").ascending();
            case "price_desc" -> Sort.by("basePrice").descending();
            case "name_asc" -> Sort.by("name").ascending();
            case "name_desc" -> Sort.by("name").descending();
            default -> Sort.by("createdAt").descending(); // newest
        };
    }
}
