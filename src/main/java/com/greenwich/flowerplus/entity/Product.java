package com.greenwich.flowerplus.entity;

import com.greenwich.flowerplus.common.constant.CommonConfig;
import com.greenwich.flowerplus.common.enums.ErrorCode;
import com.greenwich.flowerplus.common.enums.ProductStatus;
import com.greenwich.flowerplus.common.exception.DomainException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE products SET deleted_at = NOW(), slug = CONCAT(slug, '-deleted-', id) WHERE id = ?")
@Setter
@Builder
@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_slug", columnList = "slug"),
        @Index(name = "idx_product_name", columnList = "name")
})
public class Product extends BaseTsidSoftDeleteEntity {

    private static final int MAX_CATEGORIES = 5;

    //general infor
    @Column(nullable = false, length = CommonConfig.PRODUCT_NAME_LENGTH)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "care_instruction", columnDefinition = "TEXT")
    private String careInstruction;

    // Synchronized from product_assets table for backward compatibility and query performance
    // Automatically updated when assets change
    @Column(length = 500)
    private String thumbnail;

    @Column(nullable = false, unique = true, length = CommonConfig.SLUG_LENGTH)
    private String slug;

    @Column(name = "prepared_quantity")
    private int preparedQuantity;

    @Column(name = "is_make_to_order")
    private boolean isMakeToOrder;

    @Column(name = "selling_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "cost_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "original_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal originalPrice;

    // Physical attributes (shipping) - Value Object
    @Embedded
    private ShippingInfo shippingInfo;

    @Enumerated(EnumType.STRING) // Lưu chữ "ACTIVE", "DRAFT" vào DB cho dễ đọc
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arrangement_style_id")
    private ArrangementStyle arrangementStyle;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductCategory> productCategories = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductAsset> assets = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductRecipe> productRecipes = new ArrayList<>();

    @Column(name = "average_rating")
    private Double averageRating;

    @Column(name = "review_count")
    private int reviewCount;

    public void addRecipe(ProductRecipe productRecipe) {
        productRecipes.add(productRecipe);
    }

    public void removeRecipe(ProductRecipe productRecipe) {
        productRecipe.setProduct(null);
        productRecipes.remove(productRecipe);
    }

    public void addCategory(Category category) {
        // 1. Validate Business Rule: Category phải Active mới được thêm
        if (!Boolean.TRUE.equals(category.getIsActive())) {
            throw new DomainException(ErrorCode.CATEGORY_INACTIVE.getMessage());
        }

        // 2. Validate Invariant: Không quá 5 danh mục
        if (this.productCategories.size() >= MAX_CATEGORIES) {
            throw new DomainException(ErrorCode.PRODUCT_CATEGORY_LIMIT_EXCEEDED.getMessage());
        }

        // 3. Validate Duplicate: Đã có rồi thì thôi (Idempotent)
        boolean exists = this.productCategories.stream()
                .anyMatch(pc -> pc.getCategory().getId().equals(category.getId()));

        if (exists) {
            return; // Hoặc throw exception tùy gu của bạn
        }

        // 4. Action: Thực hiện liên kết
        ProductCategory productCategory = ProductCategory.builder()
                .product(this)
                .category(category)
                .build();

        this.productCategories.add(productCategory);
        // Lưu ý: Không cần add ngược vào category.getProductCategories() nếu không cần thiết
        // để tránh Lazy Loading Exception nếu list bên kia quá lớn.
    }

    public void removeCategory(Category category) {
        // 1. Validate Invariant: Sản phẩm phải thuộc ít nhất 1 danh mục
        if (this.productCategories.size() <= 1) {
            throw new DomainException(ErrorCode.PRODUCT_MUST_HAVE_CATEGORY.getMessage());
        }

        // 2. Action: Xóa (Dùng iterator hoặc removeIf)
        boolean removed = this.productCategories.removeIf(pc ->
                pc.getCategory().getId().equals(category.getId())
        );

        if (!removed) {
            throw new DomainException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND.getMessage());
        }
    }

    public void removeAllCategories() {
        // Validate: Không cho phép xóa sạch (nếu rule yêu cầu giữ ít nhất 1)
        throw new DomainException(ErrorCode.PRODUCT_MUST_HAVE_CATEGORY.getMessage());
    }

    /**
     * Activate product with business rule validation.
     * Product must have: thumbnail, at least one category, valid price.
     */
    public void activate() {
        if (this.thumbnail == null || this.thumbnail.isBlank()) {
            throw new DomainException("Product must have a thumbnail to be activated");
        }
        if (this.productCategories.isEmpty()) {
            throw new DomainException(ErrorCode.PRODUCT_MUST_HAVE_CATEGORY.getMessage());
        }
        if (this.basePrice == null || this.basePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Product must have a valid price to be activated");
        }
        this.status = ProductStatus.ACTIVE;
    }
    /**
     * Set to draft status (no validation needed).
     */
    public void toDraft() {
        this.status = ProductStatus.DRAFT;
    }
    /**
     * Deactivate product (can always be deactivated).
     */
    public void deactivate() {
        this.status = ProductStatus.INACTIVE;
    }

    public void updateBasePrice(BigDecimal newPrice) {
        Objects.requireNonNull(newPrice, "Base price cannot be null");
        if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Base price must be positive");
        }
        if (this.costPrice != null && newPrice.compareTo(this.costPrice) < 0) {
            throw new DomainException("Base price cannot be less than cost price (margin violation)");
        }
        this.basePrice = newPrice;
    }
    public void updateCostPrice(BigDecimal newCost) {
        Objects.requireNonNull(newCost, "Cost price cannot be null");
        if (newCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("Cost price cannot be negative");
        }
        this.costPrice = newCost;
    }
}
