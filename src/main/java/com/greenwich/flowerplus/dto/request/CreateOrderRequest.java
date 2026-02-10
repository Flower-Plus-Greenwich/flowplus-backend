package com.greenwich.flowerplus.dto.request;

import com.greenwich.flowerplus.common.enums.DeliveryTimeSlot;
import com.greenwich.flowerplus.dto.snapshot.ShippingAddressSnapshot;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateOrderRequest(

        @NotNull(message = "Địa chỉ giao hàng là bắt buộc")
        @Valid // Validate các field bên trong ShippingAddressSnapshot
        ShippingAddressSnapshot shippingAddress,

        @NotNull(message = "Phương thức thanh toán là bắt buộc")
        String paymentMethod,

        DeliveryTimeSlot deliveryTimeSlot,

        String customerNote,

        @NotEmpty(message = "Đơn hàng không được trống")
        @Valid // Quan trọng: Validate từng item bên trong List
        List<OrderItemRequest> items

) {}