package com.greenwich.flowerplus.entity;

import com.greenwich.flowerplus.common.enums.OrderItemType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "order_details",
        indexes = {
                @Index(name = "idx_order_detail_order_id", columnList = "order_id"),
                @Index(name = "idx_order_detail_product_id", columnList = "product_id")
        }
)
public class OrderDetail extends BaseTsidEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id")
    private Long productId; // NULL = custom order

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", length = 20, nullable = false)
    private OrderItemType itemType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "unit_cost", precision = 15, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "line_total", precision = 15, scale = 2, nullable = false)
    private BigDecimal lineTotal;

    @Column(name = "custom_config", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String customConfig;

    @Override
    protected void onPrePersist() {
        // Gọi cha để sinh ID (nếu chưa có) & Audit created_at
        super.onPrePersist();

        // Tính tiền
        this.calculateLineTotal();
    }

    // 2. Khi cập nhật (Update) - Ví dụ admin sửa số lượng
    @Override
    public void preUpdateAudit() {
        super.preUpdateAudit();
        // Tính lại tiền
        this.calculateLineTotal();
    }

    private void calculateLineTotal() {
        if (this.unitPrice == null || this.quantity == null) {
            return; // Tránh NullPointerException
        }

        BigDecimal discount = (this.discountAmount != null) ? this.discountAmount : BigDecimal.ZERO;

        // Công thức: (Đơn giá * Số lượng) - Giảm giá
        this.lineTotal = this.unitPrice
                .multiply(BigDecimal.valueOf(this.quantity))
                .subtract(discount);

        // Đảm bảo không âm (Business Rule)
        if (this.lineTotal.compareTo(BigDecimal.ZERO) < 0) {
            this.lineTotal = BigDecimal.ZERO;
        }
    }
}

