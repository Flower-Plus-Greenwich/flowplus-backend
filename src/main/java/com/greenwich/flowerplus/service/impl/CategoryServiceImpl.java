package com.greenwich.flowerplus.service.impl;

import com.greenwich.flowerplus.common.enums.ErrorCode;
import com.greenwich.flowerplus.common.exception.AppException;
import com.greenwich.flowerplus.common.utils.SlugUtils;
import com.greenwich.flowerplus.dto.request.CategoryRequest;
import com.greenwich.flowerplus.dto.response.CategoryPublicResponse;
import com.greenwich.flowerplus.dto.response.CategoryResponse;
import com.greenwich.flowerplus.entity.Category;
import com.greenwich.flowerplus.mapper.CategoryMapper;
import com.greenwich.flowerplus.repository.CategoryRepository;
import com.greenwich.flowerplus.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CategoryResponse createCategory(CategoryRequest request) {
        // Validate unique name
        if (categoryRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.CATEGORY_NAME_EXISTS);
        }

        // Auto-generate slug from name
        String slug = SlugUtils.toSlug(request.getName());
        if (categoryRepository.existsBySlug(slug)) {
            throw new AppException(ErrorCode.CATEGORY_SLUG_EXISTS);
        }

        // Map request to entity
        Category category = categoryMapper.toEntity(request);
        category.setSlug(slug);

        // Handle parent category
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findByIdAndDeletedAtIsNull(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_PARENT_NOT_FOUND));
            category.setParent(parent);
        }

        // Set default isActive if null
        if (category.getIsActive() == null) {
            category.setIsActive(true);
        }

        category = categoryRepository.save(category);
        log.info("Created category with id: {} and slug: {}", category.getId(), category.getSlug());

        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        // Validate unique name (excluding self)
        if (categoryRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new AppException(ErrorCode.CATEGORY_NAME_EXISTS);
        }

        // Handle parent category change
        if (request.getParentId() != null) {
            // Prevent self-referencing
            if (request.getParentId().equals(id)) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
            Category parent = categoryRepository.findByIdAndDeletedAtIsNull(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_PARENT_NOT_FOUND));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        // Update entity fields (slug is not updated - immutable after creation)
        categoryMapper.updateEntity(category, request);
        category = categoryRepository.save(category);

        log.info("Updated category with id: {}", category.getId());
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategory(Long id) {
        return categoryRepository.findByIdAndDeletedAtIsNull(id)
                .map(categoryMapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findByDeletedAtIsNullOrderByNameAsc();
        return categoryMapper.toResponseList(categories);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryPublicResponse> getActiveCategories() {
        List<Category> categories = categoryRepository.findByIsActiveTrueAndDeletedAtIsNullOrderByNameAsc();
        return categoryMapper.toPublicResponseList(categories);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryPublicResponse getCategoryForPublic(Long id) {
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(id)
                .filter(Category::getIsActive)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        return categoryMapper.toPublicResponse(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        // Check for child categories
        if (categoryRepository.existsByParentId(id)) {
            throw new AppException(ErrorCode.CATEGORY_HAS_CHILDREN);
        }

        if (category.getIsActive()) {

        }

        if (!category.canBeDeleted()) {

        }

        // Soft delete - set deletedAt timestamp
        category.setDeletedAt(Instant.now());
        categoryRepository.save(category);

        log.info("Soft deleted category with id: {}", id);
    }
}
