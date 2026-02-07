package com.greenwich.flowerplus.policy;

import com.greenwich.flowerplus.common.exception.DomainException;
import com.greenwich.flowerplus.entity.Product;

public interface ProductLifeCyclePolicy {
    void validateDelete(Product product);

    /**
     * Validates if the product satisfies all business rules to be activated.
     *
     * @param product The product to validate
     * @throws DomainException if any activation rule is violated
     */
    void validateActivation(Product product);
}
