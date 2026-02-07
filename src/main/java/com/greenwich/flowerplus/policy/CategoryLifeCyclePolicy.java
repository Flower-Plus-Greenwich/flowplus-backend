package com.greenwich.flowerplus.policy;

import com.greenwich.flowerplus.common.exception.DomainException;
import com.greenwich.flowerplus.entity.Category;

public interface CategoryLifeCyclePolicy {
    /**
     * Validates that a category can be activated.
     * <p>
     * Rules:
     * - Category must exist
     * - Parent category (if any) must be active
     *
     * @param category The category to validate for activation
     * @throws DomainException if activation rules are violated
     */
    void validateActivation(Category category);

    /**
     * Validates that a category can be deactivated.
     * <p>
     * Rules:
     * - Category must exist
     * - No active products should be using this category
     * - All children should be inactive (optional: cascade deactivation)
     *
     * @param category The category to validate for deactivation
     * @throws DomainException if deactivation rules are violated
     */
    void validateDeactivation(Category category);

    /**
     * Validates that a category can be safely deleted (soft delete).
     * <p>
     * Rules:
     * - Category must not have children (subcategories)
     * - Category must not have products assigned to it
     *
     * @param categoryId The category ID to validate for deletion
     * @throws DomainException if deletion rules are violated
     */
    void validateDelete(Long categoryId);

    /**
     * Validates that a category can be safely deleted using the entity.
     * <p>
     * This method leverages the {@link Category#canBeDeleted()} domain method.
     *
     * @param category The category entity to validate for deletion
     * @throws DomainException if deletion rules are violated
     */
    void validateDelete(Category category);
}
