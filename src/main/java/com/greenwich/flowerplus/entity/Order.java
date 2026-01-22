package com.greenwich.flowerplus.entity;

import com.greenwich.flowerplus.common.enums.OrderStatus;
import com.greenwich.flowerplus.common.enums.PaymentStatus;
import com.greenwich.flowerplus.common.utils.OrderCodeGenerator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

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
        // 1. Index để tìm đơn hàng theo mã vận đơn
        @Index(name = "idx_order_code", columnList = "order_code"),

        // 2. Index để load lịch sử đơn hàng của user cực nhanh
        @Index(name = "idx_order_user_id", columnList = "user_id"),

        // 3. Index để Admin lọc đơn theo trạng thái (VD: Lọc đơn chờ duyệt)
        @Index(name = "idx_order_status", columnList = "status"),

        // 4. Index để báo cáo doanh thu theo ngày
        @Index(name = "idx_order_created_at", columnList = "created_at")
})public class Order extends BaseTsidSoftDeleteEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile userId;

    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_address_id")
    private ContactAddress deliveryAddress;

    @Column(name = "delivery_date")
    private Instant deliveryDate;

    @Column(name = "delivery_time_slot", length = 50)
    private String deliveryTimeSlot;

    @Column(name = "recipient_name", length = 100)
    private String recipientName;

    @Column(name = "recipient_phone", length = 20)
    private String recipientPhone;

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

    @Override
    protected void onPrePersist() {
        // Gọi super để thằng cha (BaseTsid) sinh ID trước
        super.onPrePersist();

        // Lúc này this.getId() CHẮC CHẮN đã có dữ liệu (vì TSID sinh trên Java, không phải DB)
        if (this.orderCode == null && this.getId() != null) {
            this.orderCode = OrderCodeGenerator.encode(this.getId());
        }
    }

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<OrderDetail> orderDetails = new ArrayList<>();
}
