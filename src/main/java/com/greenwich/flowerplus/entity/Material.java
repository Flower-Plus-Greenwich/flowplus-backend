package com.greenwich.flowerplus.entity;

import com.greenwich.flowerplus.common.enums.MaterialType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE materials SET deleted_at = NOW() WHERE id = ?")
@Builder
@Entity
@Table(name = "materials")
public class Material extends BaseTsidSoftDeleteEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String unit;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "cost_price", precision = 15, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "selling_price", precision = 15, scale = 2)
    private BigDecimal sellingPrice;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MaterialType type;

    @OneToOne(mappedBy = "material", cascade = CascadeType.ALL)
    private MaterialStock materialStock;
}
