package com.greenwich.flowerplus.policy;

import com.greenwich.flowerplus.common.exception.DomainException;
import com.greenwich.flowerplus.entity.Product;

import java.math.BigDecimal;

public interface ProductPricingPolicy {
    /**
     * Validates if the proposed price update is allowed according to business rules.
     *
     * @param product The product being updated (state before update)
     * @param newSellingPrice The proposed selling price
     * @param newOriginalPrice The proposed original price (can be null)
     * @param newCostPrice The proposed cost price (can be null)
     * @throws DomainException if validation fails
     */
    void validatePriceUpdate(Product product, BigDecimal newSellingPrice, BigDecimal newOriginalPrice, BigDecimal newCostPrice);
}
