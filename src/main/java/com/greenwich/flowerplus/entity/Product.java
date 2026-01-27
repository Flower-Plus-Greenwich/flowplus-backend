package com.greenwich.flowerplus.entity;

import com.greenwich.flowerplus.common.constant.CommonConfig;
import com.greenwich.flowerplus.common.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
        ProductCategory productCategory = ProductCategory.builder()
                .product(this)
                .category(category)
                .build();
        productCategories.add(productCategory);
        category.getProductCategories().add(productCategory);
    }

    public void removeCategory(Category category) {
        productCategories.removeIf(pc -> pc.getCategory().equals(category));
        category.getProductCategories().removeIf(pc -> pc.getProduct().equals(this));
    }
}
