package com.greenwich.flowerplus.entity;
import com.greenwich.flowerplus.common.constant.CommonConfig;
import com.greenwich.flowerplus.common.enums.CategoryType;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    @Builder.Default
    private List<Category> children = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CategoryType type;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductCategory> productCategories = new ArrayList<>();

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

}
