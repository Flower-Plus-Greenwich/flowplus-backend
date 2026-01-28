package com.greenwich.flowerplus.service.impl;

import com.greenwich.flowerplus.common.enums.ErrorCode;
import com.greenwich.flowerplus.common.enums.MaterialType;
import com.greenwich.flowerplus.common.exception.AppException;
import com.greenwich.flowerplus.dto.request.CreateOrderRequest;
import com.greenwich.flowerplus.dto.request.OrderItemRequest;
import com.greenwich.flowerplus.dto.snapshot.OrderItemCustomConfig;
import com.greenwich.flowerplus.entity.Order;
import com.greenwich.flowerplus.entity.OrderItem;
import com.greenwich.flowerplus.entity.Product;
import com.greenwich.flowerplus.entity.UserProfile;
import com.greenwich.flowerplus.repository.ArrangementStyleRepository;
import com.greenwich.flowerplus.repository.MaterialRepository;
import com.greenwich.flowerplus.repository.OrderRepository;
import com.greenwich.flowerplus.repository.ProductRepository;
import com.greenwich.flowerplus.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final ArrangementStyleRepository arrangementStyleRepo;
    private final MaterialRepository materialRepo;

    private record ItemPricingResult(
            Product product,
            BigDecimal unitPrice,
            BigDecimal unitCost,
            OrderItemCustomConfig customConfig
    ) {
    }

    @Transactional
    public Order createOrder(CreateOrderRequest req, UserProfile currentUser) {
         Order order = initOrderHeader(req, currentUser);

         List<OrderItem> entityItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : req.items()) {
            // Tách logic tạo item ra hàm riêng
            OrderItem item = createSingleOrderItem(itemReq, order);

            entityItems.add(item);
            totalAmount = totalAmount.add(item.getSubTotal());
        }


        order.setOrderItems(entityItems);
        order.setTotalAmount(totalAmount);

        return orderRepo.save(order);
    }

    // ----------------------------------------------------------------
    // CÁC HÀM PRIVATE (HELPER METHODS)
    // ----------------------------------------------------------------

    /**
     * Hàm này chỉ lo việc khởi tạo Header của Order
     */
    private Order initOrderHeader(CreateOrderRequest req, UserProfile currentUser) {
        return Order.builder()
                .user(currentUser)
                .shippingInfo(req.shippingAddress())
                .paymentMethod(req.paymentMethod())
                .deliveryTimeSlot(req.deliveryTimeSlot())
                .customerNote(req.customerNote())
                .build();
    }

    /**
     * Hàm này là "Nhạc trưởng" cho việc tạo từng Item
     */
    private OrderItem createSingleOrderItem(OrderItemRequest itemReq, Order order) {

        // 1. Tính toán mọi thứ TRƯỚC (chưa tạo OrderItem vội)
        ItemPricingResult pricingData;

        if (itemReq.isProduct()) {
            pricingData = calculateStandardProduct(itemReq);
        } else {
            pricingData = calculateCustomItem(itemReq);
        }

        return OrderItem.builder()
                .order(order)
                .itemType(itemReq.itemType())
                .quantity(itemReq.quantity())

                // Nạp dữ liệu từ kết quả tính toán ở trên
                .product(pricingData.product())
                .unitPrice(pricingData.unitPrice())
                .unitCost(pricingData.unitCost())
                .customConfig(pricingData.customConfig())

                // KHÔNG CẦN SET SUBTOTAL, Constructor tự tính!
                .build();
    }

    // ================================
    // Logic riêng cho sản phẩm có sẵn
    //================================
    private ItemPricingResult calculateStandardProduct(OrderItemRequest itemReq) {
        Product product = productRepo.findById(itemReq.productId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        return new ItemPricingResult(
                product,
                product.getBasePrice(),
                product.getCostPrice(),
                null // Product thì ko có custom config
        );
    }

    // Thằng custom phải tính cost với price dựa vào yêu cầu nên tách ra
    private ItemPricingResult calculateCustomItem(OrderItemRequest itemReq) {
        var inputConfig = itemReq.customConfig();
        if (itemReq.customConfig() == null) throw new AppException(ErrorCode.INVALID_REQUEST,"Custom Item must have customConfig");

        // --- Style ---
        OrderItemCustomConfig.ItemSnapshot styleSnapshot = null;
        if (inputConfig.arrangementStyle() != null) {
            var style = arrangementStyleRepo.findById(inputConfig.arrangementStyle().id())
                    .orElseThrow(() -> new AppException(ErrorCode.STYLE_NOT_FOUND));

            styleSnapshot = OrderItemCustomConfig.ItemSnapshot.builder()
                    .id(style.getId())
                    .name(style.getName())
                    .unitPrice(style.getBaseLaborFee())
                    .unitCost(BigDecimal.ZERO)
                    .build();
        }

        // --- FLOWER ---
        OrderItemCustomConfig.ItemSnapshot flowerSnapshot = null;
        if (inputConfig.flowerType() != null) {
            // Tra cứu Material
            var flowerMaterial = materialRepo.findById(inputConfig.flowerType().id())
                    .orElseThrow(() -> new AppException(ErrorCode.MATERIAL_NOT_FOUND, "FLower with given ID not found"));

            if (flowerMaterial.getType() != MaterialType.FLOWER) {
                throw new AppException(ErrorCode.MATERIAL_INVALID_TYPE, "This ID is not a flower material");
            }

            flowerSnapshot = OrderItemCustomConfig.ItemSnapshot.builder()
                    .id(flowerMaterial.getId())
                    .name(flowerMaterial.getName())
                    .unitPrice(flowerMaterial.getSellingPrice())
                    .unitCost(flowerMaterial.getCostPrice())
                    .build();
        }

        // --- VASE ---
        OrderItemCustomConfig.ItemSnapshot vaseSnapshot = null;
        if (inputConfig.vase() != null && inputConfig.vase().id() != null) {
            var vaseMaterial = materialRepo.findById(inputConfig.vase().id())
                    .orElseThrow(() -> new AppException(ErrorCode.MATERIAL_NOT_FOUND, "Vase with given ID not found"));

            if (vaseMaterial.getType() != MaterialType.VASE) {
                throw new AppException(ErrorCode.MATERIAL_INVALID_TYPE, "This ID is not a vase material");
            }

            vaseSnapshot = OrderItemCustomConfig.ItemSnapshot.builder()
                    .id(vaseMaterial.getId())
                    .name(vaseMaterial.getName())
                    .unitPrice(vaseMaterial.getSellingPrice())
                    .unitCost(vaseMaterial.getCostPrice())
                    .build();
        }

        var trustedConfig = OrderItemCustomConfig.builder()
                .arrangementStyle(styleSnapshot)
                .flowerType(flowerSnapshot)
                .vase(vaseSnapshot)
                .build();

        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        // Helper tính tổng cho gọn
        if (styleSnapshot != null) {
            totalPrice = totalPrice.add(styleSnapshot.unitPrice());
            totalCost = totalCost.add(styleSnapshot.unitCost());
        }
        if (flowerSnapshot != null) {
            totalPrice = totalPrice.add(flowerSnapshot.unitPrice());
            totalCost = totalCost.add(flowerSnapshot.unitCost());
        }
        if (vaseSnapshot != null) {
            totalPrice = totalPrice.add(vaseSnapshot.unitPrice());
            totalCost = totalCost.add(vaseSnapshot.unitCost());
        }

        return new ItemPricingResult(
                null,
                totalPrice,
                totalCost,
                trustedConfig
        );
    }
}