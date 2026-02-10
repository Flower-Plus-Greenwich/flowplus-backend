package com.greenwich.flowerplus.common.enums;

import lombok.Getter;

public enum OrderStatus {
    PENDING_PAYMENT,    // (Dành cho đơn CK/VNPay) - Chưa trả tiền
    PENDING_APPROVAL,   // (Dành cho COD hoặc đã Pay xong) - Chờ Admin duyệt
    PROCESSING,         // Admin đã nhận, đang in đơn/soạn hoa
    // Tương ứng với "Out for Delivery"
    SHIPPING,           // Hoa đã giao cho Shipper
    DELIVERED,          // Shipper báo đã giao xong, khách đã cầm hoa

    COMPLETED,          // (System chốt) Sau 3 ngày không đổi trả/khiếu nại

    CANCELLED,          // Khách hủy hoặc Shop hủy
    REFUNDED;           // Trường hợp hủy sau khi đã thanh toán online
}