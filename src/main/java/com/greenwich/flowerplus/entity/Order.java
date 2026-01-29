package com.greenwich.flowerplus.entity;

import com.greenwich.flowerplus.common.enums.DeliveryTimeSlot;
import com.greenwich.flowerplus.common.enums.OrderStatus;
import com.greenwich.flowerplus.common.enums.PaymentStatus;
import com.greenwich.flowerplus.common.utils.OrderCodeGenerator;
import com.greenwich.flowerplus.common.utils.TsidUtils;
import com.greenwich.flowerplus.dto.snapshot.ShippingAddressSnapshot;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
@Setter
@Builder
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_code", columnList = "order_code"),
        @Index(name = "idx_order_user_id", columnList = "user_id"),

        // Dùng Index chỉ khi muốn tìm kiếm 1 đơn nhanh nhất thôi, chứ 2 cái dưới này mục đính thường là tìm nhiều đứa chung 1 trạng thái nên kh dùng index
//        @Index(name = "idx_order_status", columnList = "status"),
//        @Index(name = "idx_order_created_at", columnList = "created_at")
})public class Order extends BaseTsidSoftDeleteEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile user;

    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private OrderStatus status;

    @Column(name = "delivery_status", length = 30)
    private String deliveryStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20)
    private PaymentStatus paymentStatus;

    @Column(name = "payment_method", length = 30)
    private String paymentMethod;

    //TODO: Chơi snapshot địa chỉ khi đặt hàng
    // Vì thế nên sẽ kh dùng FK ở đây, thay vào đó dùng JSONB
    // ContactAddress đã có RecipientName, RecipientPhone rồi
    // Lấy tù ContactAddress ra theo cách copy cột, kh dùng FK
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="shipping_address", columnDefinition = "jsonb", nullable = false)
    private ShippingAddressSnapshot shippingInfo;

    @Column(name = "delivery_date")
    private Instant deliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_time_slot", length = 50)
    private DeliveryTimeSlot deliveryTimeSlot;

    @Column(name = "customer_note", columnDefinition = "TEXT")
    private String customerNote;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "order_code", unique = true, length = 20, nullable = false)
    private String orderCode;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> orderItems = new ArrayList<>();
}
