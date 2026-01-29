package com.greenwich.flowerplus.service.impl;

import com.greenwich.flowerplus.common.enums.ErrorCode;
import com.greenwich.flowerplus.common.enums.TransactionType;
import com.greenwich.flowerplus.common.exception.AppException;
import com.greenwich.flowerplus.dto.internal.OrderInventoryRequest;
import com.greenwich.flowerplus.dto.internal.OrderItemSnapshot;
import com.greenwich.flowerplus.dto.request.MaterialStockDeductRequest;
import com.greenwich.flowerplus.entity.Product;
import com.greenwich.flowerplus.entity.ProductRecipe;
import com.greenwich.flowerplus.repository.ProductRepository;
import com.greenwich.flowerplus.service.InventoryFacade;
import com.greenwich.flowerplus.service.MaterialStockService;
import com.greenwich.flowerplus.service.ProductRecipeService;
import com.greenwich.flowerplus.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryFacadeImpl implements InventoryFacade {

    private final MaterialStockService materialStockService;
    private final ProductRepository productRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reserveStock(OrderInventoryRequest request) {
        log.info("RESERVING stock for Order: {}", request.orderCode());

        // 1. Tính toán nguyên liệu (Bóc tách Recipe)
        Map<Long, Integer> materialsNeeded = calculateMaterialRequirements(request);

        // 2. Gọi Service Kho để GIỮ CHỖ (Reserve)
        // Nếu kho không đủ Available -> Service sẽ ném lỗi -> Rollback đơn hàng ngay lập tức
        for (Map.Entry<Long, Integer> entry : materialsNeeded.entrySet()) {
            materialStockService.reserveForOrder(
                    entry.getKey(),     // Material ID
                    entry.getValue(),   // Quantity
                    request.orderCode() // Order Code
            );
        }
        log.info("Stock RESERVED successfully for Order {}", request.orderCode());
    }

    // =================================================================
    // PHA 2: CHỐT ĐƠN (Gọi khi thanh toán thành công / Webhook)
    // =================================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmStock(OrderInventoryRequest request) {
        log.info("CONFIRMING stock deduction for Order: {}", request.orderCode());

        // 1. Tính toán lại (để đảm bảo khớp với lúc Reserve)
        Map<Long, Integer> materialsNeeded = calculateMaterialRequirements(request);

        // 2. Gọi Service Kho để TRỪ THẬT (Deduct & Clear Reserve)
        for (Map.Entry<Long, Integer> entry : materialsNeeded.entrySet()) {
            materialStockService.confirmReservation(
                    entry.getKey(),
                    entry.getValue(),
                    request.orderCode()
            );
        }
        log.info("Stock DEDUCTED (Confirmed) for Order {}", request.orderCode());
    }

    // =================================================================
    // PHA 3: HỦY/HOÀN TÁC (Gọi khi thanh toán thất bại / Hủy đơn)
    // =================================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseStock(OrderInventoryRequest request, String reason) {
        log.info("RELEASING stock for Order: {}", request.orderCode());

        Map<Long, Integer> materialsNeeded = calculateMaterialRequirements(request);

        for (Map.Entry<Long, Integer> entry : materialsNeeded.entrySet()) {
            try {
                materialStockService.releaseReservation(
                        entry.getKey(),
                        entry.getValue(),
                        request.orderCode(),
                        reason
                );
            } catch (Exception e) {
                // Log error nhưng không throw exception để tránh chặn luồng hủy đơn
                log.error("Failed to release stock for material {}: {}", entry.getKey(), e.getMessage());
            }
        }
        log.info("Stock RELEASED for Order {}", request.orderCode());
    }

    // =================================================================
    // HELPER: Logic bóc tách công thức (Dùng chung cho cả 3 hàm)
    // =================================================================
    private Map<Long, Integer> calculateMaterialRequirements(OrderInventoryRequest request) {
        Map<Long, Integer> totalMaterialsNeeded = new HashMap<>();

        for (OrderItemSnapshot item : request.items()) {
            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

            if (product.getProductRecipes() == null || product.getProductRecipes().isEmpty()) {
                continue;
            }

            for (ProductRecipe recipe : product.getProductRecipes()) {
                Long materialId = recipe.getMaterial().getId();
                int qtyNeeded = item.quantity() * recipe.getQuantityNeeded();
                totalMaterialsNeeded.merge(materialId, qtyNeeded, Integer::sum);
            }
        }
        return totalMaterialsNeeded;
    }
}
