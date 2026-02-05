    package com.greenwich.flowerplus.service.impl;

    import com.greenwich.flowerplus.common.enums.ErrorCode;
    import com.greenwich.flowerplus.common.exception.AppException;
    import com.greenwich.flowerplus.dto.request.CartAddItemRequest;
    import com.greenwich.flowerplus.dto.response.CartResponse;
    import com.greenwich.flowerplus.entity.Cart;
    import com.greenwich.flowerplus.entity.CartItem;
    import com.greenwich.flowerplus.entity.Product;
    import com.greenwich.flowerplus.entity.UserProfile;
    import com.greenwich.flowerplus.mapper.CartMapper;
    import com.greenwich.flowerplus.repository.CartItemRepository;
    import com.greenwich.flowerplus.repository.CartRepository;
    import com.greenwich.flowerplus.repository.ProductRepository;
    import com.greenwich.flowerplus.repository.UserProfileRepository;
    import com.greenwich.flowerplus.service.CartService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.time.Instant;
    import java.time.temporal.ChronoUnit;
    import java.util.Optional;
    import java.util.UUID;

    @Service
    @RequiredArgsConstructor
    public class CartServiceImpl implements CartService {
        private final CartRepository cartRepository;
        private final CartItemRepository cartItemRepository;
        private final UserProfileRepository userProfileRepository;
        private final ProductRepository productRepository;
        private final CartMapper cartMapper;

        @Override
        @Transactional(readOnly = true)
        public CartResponse getCart(Long userId, String token) {
            Cart cart = getCartEntity(userId, token);
//            System.out.println("UserId: " + userId + ", Token: " + token);


            // Cart null
            if (cart == null) {
                return CartResponse.builder().totalItems(0).build();
            }

            System.out.println("Cart ID: " + cart.getId());

            return cartMapper.toResponse(cart);
        }

        @Override
        @Transactional
        public CartResponse addToCart(Long userId, CartAddItemRequest request) {
            Cart cart = resolveCart(userId, request.getCartToken());

            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

            Optional<CartItem> existingItemOpt = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());

            if (existingItemOpt.isPresent()) {
                CartItem existingItem = existingItemOpt.get();
                existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
                cartItemRepository.save(existingItem);
            } else {
                CartItem newItem = CartItem.builder()
                        .cart(cart)
                        .product(product)
                        .quantity(request.getQuantity())
                        .build();
                cartItemRepository.save(newItem);

                cart.getItems().add(newItem);
            }

            return cartMapper.toResponse(cart);
        }

        @Override
        @Transactional
        public CartResponse updateCart(Long userId, CartAddItemRequest request) {
            Cart cart = getCartEntity(userId, request.getCartToken());


            System.out.println("Update Cart - UserId: " + userId + ", Token: " + request.getCartToken());
            System.out.println("Cart ID: " + cart.getId());
            System.out.println("Request Product ID: " + request.getProductId() + ", Quantity: " + request.getQuantity());


            CartItem itemToUpdate = cart.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(request.getProductId()))
                    .findFirst()
                    .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

            if (request.getQuantity() <= 0) {
                cart.getItems().remove(itemToUpdate);
                cartItemRepository.delete(itemToUpdate);
            } else {
                //TODO: Quantity của Product lấy ở đâu
//                if (itemToUpdate.getProduct() < request.getQuantity()) {
//                    throw new AppException(ErrorCode.OUT_OF_STOCK);
//                }

                itemToUpdate.setQuantity(request.getQuantity());
                cartItemRepository.save(itemToUpdate);
            }

            return cartMapper.toResponse(cart);
        }


        // PRIVATE METHODS

        private Cart getCartEntity(Long userId, String token) {
            if (userId != null) {
                return cartRepository.findByUserId(userId)
                        .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
            }

            if (token != null) {
                return cartRepository.findByCartToken(token)
                        .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
            }

            throw new AppException(ErrorCode.CART_NOT_FOUND);
        }

        private Cart resolveCart(Long userId, String token) {
            if (userId != null) {
                return cartRepository.findByUserId(userId)
                        .orElseGet(() -> createNewCartForUser(userId));
            }

            if (token != null && cartRepository.existsByCartToken(token)) {
                return cartRepository.findByCartToken(token)
                        .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
            }

            return createNewGuestCart();
        }

        private Cart createNewCartForUser(Long userId) {
            UserProfile user = userProfileRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            Cart cart = Cart.builder().user(user).build();
            return cartRepository.save(cart);
        }

        private Cart createNewGuestCart() {
            Cart cart = Cart.builder()
                    .cartToken(UUID.randomUUID().toString())
                     .expireAt(Instant.now().plus(7, ChronoUnit.DAYS))
                    .build();
            return cartRepository.save(cart);
        }

//        private boolean checkQuantityAvailable(Product product, Integer requestedQuantity) {
//            return product.get >= requestedQuantity;
//        }
    }
