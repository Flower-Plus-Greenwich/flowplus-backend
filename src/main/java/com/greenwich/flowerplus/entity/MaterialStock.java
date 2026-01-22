package com.greenwich.flowerplus.entity;

import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "material_stocks")
public class MaterialStock extends BaseTsidEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private Material material;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;

    @Column(name = "opening_balance", nullable = false)
    @Builder.Default
    private Integer openingBalance = 0;

    @Column(name = "reorder_level", columnDefinition = "INTEGER DEFAULT 10")
    @Builder.Default
    private Integer reorderLevel = 10;

    public int getAvailableStock() {
        return this.quantity - this.reservedQuantity;
    }
}
