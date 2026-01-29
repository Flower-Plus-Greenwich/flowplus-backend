package com.greenwich.flowerplus.entity;

import com.greenwich.flowerplus.common.enums.OrderItemType;
import com.greenwich.flowerplus.dto.snapshot.OrderItemCustomConfig;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//@Builder
@Entity
@Table(
        name = "order_items",
        indexes = {
                @Index(name = "idx_order_item_order_id", columnList = "order_id")
        }
)
// Logic:
// 1. Nếu là PRODUCT -> productId phải có.
// 2. Nếu là CUSTOM -> productId phải null VÀ customConfig phải có dữ liệu.
@Check(constraints = "((item_type = 'PRODUCT' AND product_id IS NOT NULL) OR (item_type = 'CUSTOM' AND product_id IS NULL AND custom_config IS NOT NULL))")
public class OrderItem extends BaseTsidSoftDeleteEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product; // NULL = custom order

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", length = 20, nullable = false)
    private OrderItemType itemType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "unit_cost", precision = 15, scale = 2)
    private BigDecimal unitCost;

    // Chặn Setter từ bên ngoài để đảm bảo toàn vẹn dữ liệu
    @Setter(AccessLevel.NONE)
    @Column(name = "sub_total", precision = 15, scale = 2, nullable = false)
    private BigDecimal subTotal;

    //TODO: Nên thống nhất 1 thằng lại để thống nhất Format
    @Column(name = "custom_config", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private OrderItemCustomConfig customConfig;

    // Nếu mà m kh muốn đụng vô logic tính toán subtotal ở PrePersit luôn thì có trò này
    // Tạo Constructor chứa các tham số CẦN THIẾT để tạo object, nó chỉ tạo builder cho mấy tham số này thôi
        @Builder
        public OrderItem(Order order, Product product, OrderItemType itemType,
                         Integer quantity, BigDecimal unitPrice, BigDecimal unitCost,
                         OrderItemCustomConfig customConfig) {
            this.order = order;
            this.product = product;
            this.itemType = itemType;
            this.quantity = quantity;
            this.unitCost = unitCost;
            this.customConfig = customConfig;
            // Nếu mà là Product thì gán giá
            if (unitPrice != null) {
                this.unitPrice = unitPrice;
                this.subTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            } else {
                // Trường hợp custom thì để 0, update sau
                this.unitPrice = BigDecimal.ZERO;
                this.subTotal = BigDecimal.ZERO;
            }
        }
    // Khi này thì lúc dùng builder kh cần set subtotal nữa, mà khi gọi .build() nó sẽ tự tính luôn
    public void updateCustomPrice(BigDecimal newPrice) {
        if (!this.itemType.equals(OrderItemType.CUSTOM)) {
            throw new IllegalStateException("Chỉ được cập nhật giá cho đơn hàng Custom");
        }
        this.unitPrice = newPrice;
        this.subTotal = newPrice.multiply(BigDecimal.valueOf(this.quantity));
    }

    public boolean isCustomOrder() {
        return OrderItemType.CUSTOM.equals(this.itemType);
    }
}

