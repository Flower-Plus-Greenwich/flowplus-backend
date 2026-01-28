package com.greenwich.flowerplus.common.enums;

public enum TransactionType {
    IMPORT,       // Nhập kho nguyên liệu
    SALE,         // Bán hàng (xuất kho cho đơn hàng)
    USAGE,        // Sử dụng cho sản xuất
    DAMAGED,      // Hư hỏng
    ADJUST_UP,    // Điều chỉnh tăng (kiểm kê phát hiện thừa)
    ADJUST_DOWN,  // Điều chỉnh giảm (kiểm kê phát hiện thiếu)
    RETURN,       // Trả hàng về kho
    RESERVE,      // Đặt trước cho đơn hàng pending
    RELEASE       // Giải phóng đặt trước (đơn hàng bị hủy)
}
