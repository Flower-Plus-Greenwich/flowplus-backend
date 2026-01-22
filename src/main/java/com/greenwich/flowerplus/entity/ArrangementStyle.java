package com.greenwich.flowerplus.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "arrangement_styles")
public class ArrangementStyle extends BaseTsidSoftDeleteEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "base_labor_fee", precision = 15, scale = 2)
    private BigDecimal baseLaborFee;

    @OneToMany(mappedBy = "arrangementStyle", fetch = FetchType.LAZY)
    private List<Product> products;
}
