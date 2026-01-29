package com.greenwich.flowerplus.service;

import com.greenwich.flowerplus.dto.request.CreateOrderRequest;
import com.greenwich.flowerplus.entity.Order;
import com.greenwich.flowerplus.entity.UserProfile;

public interface OrderService {
    Order createOrder(CreateOrderRequest req, UserProfile currentUser);
}
