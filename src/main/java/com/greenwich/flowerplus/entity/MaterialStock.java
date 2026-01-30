package com.greenwich.flowerplus.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;


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

    // Giá vốn trung bình (Moving Average Cost)
    // Cực kỳ quan trọng để tính lãi lỗ.
    // Công thức: ((Giá cũ * Tồn cũ) + (Giá nhập mới * Lượng nhập mới)) / (Tồn cũ + Lượng nhập mới)
    @Column(name = "cost_price", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal costPrice = BigDecimal.ZERO;

    @Column(name = "reorder_level", columnDefinition = "INTEGER DEFAULT 10")
    @Builder.Default
    private Integer reorderLevel = 10;

    public int getAvailableStock() {
        return this.quantity - this.reservedQuantity;
    }

    public BigDecimal getTotalValue() {
        return this.costPrice.multiply(BigDecimal.valueOf(this.quantity));
    }
}
