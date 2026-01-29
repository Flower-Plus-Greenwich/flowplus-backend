package com.greenwich.flowerplus.service;

import com.greenwich.flowerplus.dto.internal.OrderInventoryRequest;

import java.util.List;

public interface InventoryFacade {

    void reserveStock(OrderInventoryRequest request);

    void confirmStock(OrderInventoryRequest request);

    void releaseStock(OrderInventoryRequest request, String reason);

}
