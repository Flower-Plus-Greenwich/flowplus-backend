package com.greenwich.flowerplus.service.impl;

import com.greenwich.flowerplus.common.enums.ProductStatus;
import com.greenwich.flowerplus.dto.request.ProductSearchRequest;
import com.greenwich.flowerplus.dto.response.ProductListingDto;
import com.greenwich.flowerplus.dto.response.ProductResponseAdmin;
import com.greenwich.flowerplus.entity.Product;
import com.greenwich.flowerplus.mapper.ProductMapper;
import com.greenwich.flowerplus.repository.ProductRepository;
import com.greenwich.flowerplus.service.ProductSearchService;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * ProductSearchServiceImpl - Single Responsibility: Product Search Operations
 * <p>
 * Handles all search-related operations for products including:
 * - Public search (customer-facing storefront)
 * - Admin search (backoffice with all status filters)
 * <p>
 * Supports:
 * - Multiple category filtering (OR logic)
 * - Price range filtering
 * - Keyword search
 * - Multiple sort options
 * <p>
 * This service is separated from ProductServiceImpl following SOLID principles.
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    private static final String BASE_PRICE = "basePrice";

    // ============================================================================
    // PUBLIC SEARCH (Customer - Storefront)
    // ============================================================================

    /**
     * Public search - forces ACTIVE status only
     * Used by customer-facing storefront
     * <p>
     * Returns lightweight ProductListingDto for better performance
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductListingDto> searchPublic(ProductSearchRequest req) {
        log.debug("Public search - keyword: {}, categoryIds: {}", req.getKeyword(), req.getCategoryIds());

        // Force ACTIVE status for public/customer searches
        req.setStatus(ProductStatus.ACTIVE);

        Page<Product> productPage = executeSearch(req);

        log.debug("Public search found {} products", productPage.getTotalElements());

        return productPage.map(productMapper::toListingDto);
    }

    // ============================================================================
    // ADMIN SEARCH (Backoffice)
    // ============================================================================

    /**
     * Admin search - respects status parameter
     * If no filters provided, returns all products with pagination
     * <p>
     * Returns detailed ProductResponseAdmin for backoffice operations
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseAdmin> searchAdmin(ProductSearchRequest req) {
        log.debug("Admin search - keyword: {}, status: {}", req.getKeyword(), req.getStatus());

        Page<Product> productPage = executeSearch(req);

        log.debug("Admin search found {} products", productPage.getTotalElements());

        return productPage.isEmpty() 
                ? Page.empty(buildPageable(req)) 
                : productPage.map(productMapper::toAdminDto);
    }

    // ============================================================================
    // CORE SEARCH EXECUTION
    // ============================================================================

    private Page<Product> executeSearch(ProductSearchRequest req) {
        Specification<Product> spec = buildSpecification(req);
        Pageable pageable = buildPageable(req);
        return productRepository.findAll(spec, pageable);
    }

    // ============================================================================
    // SPECIFICATION BUILDER (Refactored for low complexity)
    // ============================================================================

    /**
     * Builds JPA Specification with support for:
     * - keyword (name search with LIKE)
     * - Single or multiple categoryIds (OR logic)
     * - Single or multiple categorySlugs (OR logic)
     * - minPrice / maxPrice
     * - status
     */
    private Specification<Product> buildSpecification(ProductSearchRequest req) {
        return (root, query, cb) -> {
            query.distinct(true);

            List<Predicate> predicates = new ArrayList<>();

            addKeywordPredicate(req.getKeyword(), root, cb).ifPresent(predicates::add);
            addCategoryPredicate(req, root, cb).ifPresent(predicates::add);
            addPricePredicates(req.getMinPrice(), req.getMaxPrice(), root, cb, predicates);
            addStatusPredicate(req.getStatus(), root, cb).ifPresent(predicates::add);

            return predicates.isEmpty() 
                    ? cb.conjunction() 
                    : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // ============================================================================
    // PREDICATE BUILDERS (Single Responsibility per method)
    // ============================================================================

    private Optional<Predicate> addKeywordPredicate(String keyword, Root<Product> root, CriteriaBuilder cb) {
        if (!StringUtils.hasText(keyword)) {
            return Optional.empty();
        }
        String pattern = "%" + keyword.toLowerCase() + "%";
        return Optional.of(cb.like(cb.lower(root.get("name")), pattern));
    }

    private Optional<Predicate> addCategoryPredicate(ProductSearchRequest req, Root<Product> root, CriteriaBuilder cb) {
        List<Predicate> categoryPredicates = new ArrayList<>();

        // Multiple category IDs
        parseCategoryIds(req.getCategoryIds())
                .ifPresent(ids -> categoryPredicates.add(buildCategoryIdInPredicate(ids, root)));

        // Single category ID (legacy)
        if (categoryPredicates.isEmpty()) {
            parseSingleCategoryId(req.getCategoryId())
                    .ifPresent(id -> categoryPredicates.add(buildCategoryIdEqualPredicate(id, root, cb)));
        }

        // Multiple category slugs
        parseCategorySlugs(req.getCategorySlugs())
                .ifPresent(slugs -> categoryPredicates.add(buildCategorySlugInPredicate(slugs, root)));

        // Single category slug (legacy)
        if (categoryPredicates.isEmpty() && StringUtils.hasText(req.getCategorySlug())) {
            categoryPredicates.add(buildCategorySlugEqualPredicate(req.getCategorySlug().trim(), root, cb));
        }

        if (categoryPredicates.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(cb.or(categoryPredicates.toArray(new Predicate[0])));
    }

    private void addPricePredicates(BigDecimal minPrice, BigDecimal maxPrice, 
                                     Root<Product> root, CriteriaBuilder cb, 
                                     List<Predicate> predicates) {
        if (minPrice != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get(BASE_PRICE), minPrice));
        }
        if (maxPrice != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get(BASE_PRICE), maxPrice));
        }
    }

    private Optional<Predicate> addStatusPredicate(ProductStatus status, Root<Product> root, CriteriaBuilder cb) {
        if (status == null) {
            return Optional.empty();
        }
        return Optional.of(cb.equal(root.get("status"), status));
    }

    // ============================================================================
    // CATEGORY PREDICATE HELPERS
    // ============================================================================

    private Predicate buildCategoryIdInPredicate(List<Long> ids, Root<Product> root) {
        var categoryJoin = joinCategory(root);
        return categoryJoin.get("id").in(ids);
    }

    private Predicate buildCategoryIdEqualPredicate(Long id, Root<Product> root, CriteriaBuilder cb) {
        var categoryJoin = joinCategory(root);
        return cb.equal(categoryJoin.get("id"), id);
    }

    private Predicate buildCategorySlugInPredicate(List<String> slugs, Root<Product> root) {
        var categoryJoin = joinCategory(root);
        return categoryJoin.get("slug").in(slugs);
    }

    private Predicate buildCategorySlugEqualPredicate(String slug, Root<Product> root, CriteriaBuilder cb) {
        var categoryJoin = joinCategory(root);
        return cb.equal(categoryJoin.get("slug"), slug);
    }

    private Path<?> joinCategory(Root<Product> root) {
        return root.join("productCategories", JoinType.INNER).join("category", JoinType.INNER);
    }

    // ============================================================================
    // PARSING HELPERS
    // ============================================================================

    private Optional<List<Long>> parseCategoryIds(List<String> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Optional.empty();
        }

        List<Long> parsedIds = categoryIds.stream()
                .filter(StringUtils::hasText)
                .map(this::parseId)
                .filter(Objects::nonNull)
                .toList();

        return parsedIds.isEmpty() ? Optional.empty() : Optional.of(parsedIds);
    }

    private Optional<Long> parseSingleCategoryId(String categoryId) {
        if (!StringUtils.hasText(categoryId)) {
            return Optional.empty();
        }
        return Optional.ofNullable(parseId(categoryId.trim()));
    }

    private Long parseId(String id) {
        try {
            return Long.valueOf(id.trim());
        } catch (NumberFormatException _) {
            log.warn("Invalid categoryId format: {}", id);
            return null;
        }
    }

    private Optional<List<String>> parseCategorySlugs(List<String> categorySlugs) {
        if (categorySlugs == null || categorySlugs.isEmpty()) {
            return Optional.empty();
        }

        List<String> trimmedSlugs = categorySlugs.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .toList();

        return trimmedSlugs.isEmpty() ? Optional.empty() : Optional.of(trimmedSlugs);
    }

    // ============================================================================
    // PAGINATION BUILDER
    // ============================================================================

    private Pageable buildPageable(ProductSearchRequest req) {
        int page = Math.max(0, req.getPage() - 1);
        Sort sort = buildSort(req.getSort() != null ? req.getSort().getValue() : null);
        return PageRequest.of(page, req.getSize(), sort);
    }

    private Sort buildSort(String sortParam) {
        return switch (sortParam != null ? sortParam : "newest") {
            case "price_asc" -> Sort.by(BASE_PRICE).ascending();
            case "price_desc" -> Sort.by(BASE_PRICE).descending();
            case "name_asc" -> Sort.by("name").ascending();
            case "name_desc" -> Sort.by("name").descending();
            case "rating" -> Sort.by("averageRating").descending();
            case "popular" -> Sort.by("reviewCount").descending();
            default -> Sort.by("createdAt").descending();
        };
    }
}
