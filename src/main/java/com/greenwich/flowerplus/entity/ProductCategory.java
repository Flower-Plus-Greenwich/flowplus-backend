package com.greenwich.flowerplus.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product_categories", indexes = {
        @Index(name = "idx_product_category_product", columnList = "product_id"),
        @Index(name = "idx_product_category_category", columnList = "category_id")
})
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE product_categories SET deleted_at = NOW() WHERE id = ?")
public class ProductCategory extends BaseTsidSoftDeleteEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

}
