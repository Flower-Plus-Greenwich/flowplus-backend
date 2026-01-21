package com.greenwich.flowerplus.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product_recipes", uniqueConstraints = {
        @UniqueConstraint(name = "unique_constraint_product_recipe",columnNames = {"product_id", "material_id"})
})
public class ProductRecipe extends BaseTsidEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(name = "quantity_needed", nullable = false)
    private int quantityNeeded;

}
