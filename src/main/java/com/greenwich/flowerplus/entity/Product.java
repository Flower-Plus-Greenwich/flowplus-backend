package com.greenwich.flowerplus.entity;

import com.greenwich.flowerplus.common.constant.CommonConfig;
import com.greenwich.flowerplus.common.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
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

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal basePrice;

    @Enumerated(EnumType.STRING) // Lưu chữ "ACTIVE", "DRAFT" vào DB cho dễ đọc
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arrangement_style_id")
    private ArrangementStyle arrangementStyle;

    @ManyToMany(mappedBy = "products", fetch = FetchType.LAZY)
    private List<Category> categories;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductAsset> assets;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductRecipe> productRecipes;

    @Column(name = "average_rating")
    private Double averageRating;

    @Column(name = "review_count")
    private int reviewCount;


}
