package com.greenwich.flowerplus.controller;

import com.greenwich.flowerplus.common.ApiResult;
import com.greenwich.flowerplus.dto.request.CartAddItemRequest;
import com.greenwich.flowerplus.dto.request.CartUpdateRequest;
import com.greenwich.flowerplus.dto.response.CartResponse;
import com.greenwich.flowerplus.infrastructure.security.SecurityUserDetails;
import com.greenwich.flowerplus.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/carts")
public class CartController {
    private final CartService cartService;

    @Operation(summary = "Lấy chi tiết giỏ hàng (Nếu là guest thì dùng cart token), là user thì lấy theo userId")
    @GetMapping
    public ResponseEntity<ApiResult<CartResponse>> getCart(
            @AuthenticationPrincipal SecurityUserDetails currentUser,

            @RequestHeader(value = "X-Cart-Token", required = false) String cartTokenHeader
    ) {
        Long userId = (currentUser != null) ? currentUser.getUser().getId() : null;
        String token = cartTokenHeader; // Token từ header

        log.info("Get Cart - UserId: {}, Token: {}", userId, token);

        // Gọi Service xử lý logic ưu tiên: Có User -> Lấy theo User, Không -> Lấy theo Token
        CartResponse cart = cartService.getCart(userId, token);


        return ResponseEntity.ok(ApiResult.success(cart, "Cart retrieved successfully"));

    }

    @Operation(summary = "Thêm sản phẩm vào giỏ")
    @PostMapping("/items")
    public ResponseEntity<ApiResult<CartResponse>> addToCart(
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @Valid @RequestBody CartAddItemRequest request
    ) {
        // A. Xác định người dùng
        Long userId = (currentUser != null) ? currentUser.getUser().getId() : null;

        // B. Xác định token (Lấy từ request body nếu FE gửi lên)
        String token = request.getCartToken();

        log.info("Add to Cart - UserId: {}, ProductId: {}, Token: {}", userId, request.getProductId(), token);

        CartResponse updatedCart = cartService.addToCart(userId, request);

        return ResponseEntity.ok(ApiResult.success(updatedCart, "Added to cart successfully"));
    }

    @PutMapping
    public ResponseEntity<ApiResult<CartResponse>> updateCart(
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @Valid @RequestBody CartAddItemRequest request
    ) {
        Long userId = (currentUser != null) ? currentUser.getUser().getId() : null;

        String token = request.getCartToken();

        log.info("Update Cart - UserId: {}, ProductId: {}, Token: {}", userId, request.getProductId(), token);

        CartResponse updatedCart = cartService.updateCart(userId, request);

        return ResponseEntity.ok(ApiResult.success(updatedCart, "Cart updated successfully"));
    }


    // ========================================================================
    // 3. XÓA ITEM / CẬP NHẬT SỐ LƯỢNG (Tương tự)
    // ========================================================================
//    @DeleteMapping("/items/{itemId}")
//    public ResponseEntity<ApiResult<CartResponse>> removeFromCart(
//            @AuthenticationPrincipal CustomUserDetails currentUser,
//            @PathVariable Long itemId,
//            @RequestParam(required = false) String cartToken // Guest phải gửi kèm token để verify sở hữu
//    ) {
//        Long userId = (currentUser != null) ? currentUser.getId() : null;
//        CartResponse updatedCart = cartService.removeItem(userId, cartToken, itemId);
//        return ResponseEntity.ok(ApiResult.success(updatedCart, "Item removed"));
//    }
}
