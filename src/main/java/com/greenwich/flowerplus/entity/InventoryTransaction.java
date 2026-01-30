package com.greenwich.flowerplus.entity;

import com.greenwich.flowerplus.common.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "inventory_transactions")
public class InventoryTransaction extends BaseTsidEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    /**
     * The quantity change (SIGNED INTEGER).
     * - Positive: stock added (IMPORT, RETURN, ADJUST_UP)
     * - Negative: stock removed (SALE, ADJUST_DOWN, DAMAGED)
     * - Zero: reservation changes only (RESERVE, RELEASE)
     */
    @Column(name = "quantity_delta", nullable = false)
    private Integer quantityDelta;

    /**
     * Quantity BEFORE this transaction was applied.
     * Used for audit and reconciliation.
     */
    @Column(name = "before_quantity", nullable = false)
    private Integer beforeQuantity;

    /**
     * Quantity AFTER this transaction was applied.
     * Should equal beforeQuantity + quantityDelta (for quantity-affecting transactions).
     */
    @Column(name = "after_quantity", nullable = false)
    private Integer afterQuantity;

    /**
     * Reserved quantity BEFORE this transaction.
     * Needed for RESERVE/RELEASE transactions audit.
     */
    @Column(name = "before_reserved")
    private Integer beforeReserved;

    /**
     * Reserved quantity AFTER this transaction.
     */
    @Column(name = "after_reserved")
    private Integer afterReserved;

    // 2. Số dư SAU giao dịch (Rolling Balance)
    // Giúp truy vết lịch sử: Tại thời điểm này, kho còn bao nhiêu?
    // Ví dụ: Nhập 10 -> balance = 100. Xuất 5 -> balance = 95.
    @Column(name = "current_balance", nullable = false)
    private int currentBalance;

    // 3. Giá vốn tại thời điểm giao dịch
    // Nếu là NHẬP: Lưu giá nhập thực tế.
    // Nếu là XUẤT: Lưu giá vốn trung bình (Moving Average Price) tại thời điểm đó.
    @Column(name = "cost_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "reference_code") // Mã đơn hàng hoặc Mã phiếu nhập
    private String referenceCode;

    // 4. Ghi chú chi tiết
    // VD: "Hỏng do vận chuyển", "Nhập hàng nhà cung cấp A"
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type; // IMPORT, EXPORT_SALE, EXPORT_DAMAGE, RETURN...
}