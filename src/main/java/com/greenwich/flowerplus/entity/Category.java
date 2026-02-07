package com.greenwich.flowerplus.entity;
import com.greenwich.flowerplus.common.constant.CommonConfig;
import com.greenwich.flowerplus.common.enums.CategoryType;
import com.greenwich.flowerplus.common.enums.ErrorCode;
import com.greenwich.flowerplus.common.enums.ProductStatus;
import com.greenwich.flowerplus.common.exception.DomainException;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "categories",
        indexes = {
                // Khai báo rõ ràng index ở đây cho dễ quản lý
                @Index(name = "idx_category_slug", columnList = "slug"),
                @Index(name = "idx_category_name", columnList = "name")
        }
)
public class Category extends BaseTsidSoftDeleteEntity {

    // Rule 1: Tên danh mục không được trùng, không null, max 100 ký tự (tiết kiệm hơn 255)
    @Column(nullable = false, unique = true, length = CommonConfig.NAME_LENGTH)
    private String name;

    // Unique = true -> Đã có index ngầm, nhưng khai báo ở @Table cho tường minh
    // Rule 2: Slug bắt buộc unique để làm SEO URL, index luôn cho nhanh
    @Column(nullable = false, unique = true, length = CommonConfig.SLUG_LENGTH)
    private String slug; // Cần thiết cho URL: /c/quan-ao-nam

    @Column(columnDefinition = "TEXT", length = CommonConfig.SHORT_DESC_LENGTH)
    private String description;

    @Column(name = "thumbnail")
    private String thumbnail;   

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    @Builder.Default
    private List<Category> children = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CategoryType type;

    @OneToMany(mappedBy = "category")
    @Builder.Default
    private List<ProductCategory> productCategories = new ArrayList<>();

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Check if this category can be safely deleted.
     * @return true if no products reference this category
     */
    public boolean canBeDeleted() {
        return this.productCategories.isEmpty() && this.children.isEmpty();
    }
    /**
     * Deactivate this category with business rule validation.
     * @throws DomainException if active products use this category
     */
    public void deactivate() {
        if (!this.productCategories.isEmpty()) {
            boolean hasActiveProducts = this.productCategories.stream()
                    .anyMatch(pc -> pc.getProduct().getStatus() == ProductStatus.ACTIVE);
            if (hasActiveProducts) {
                throw new DomainException(ErrorCode.CATEGORY_HAS_ACTIVE_PRODUCTS.getMessage());
            }
        }
        this.isActive = false;
    }
    /**
     * Activate this category.
     */
    public void activate() {
        this.isActive = true;
    }

}
