-- ================================================================
-- PHẦN 1: CẬP NHẬT TYPE CHO CÁC BẢNG CŨ (ORDERS, ORDER_ITEMS)
-- Chuyển đổi từ BIGINT sang VARCHAR(255)
-- ================================================================

-- 1. Update bảng ORDERS
ALTER TABLE orders
ALTER COLUMN created_by TYPE VARCHAR(255),
    ALTER COLUMN updated_by TYPE VARCHAR(255),
    ALTER COLUMN deleted_by TYPE VARCHAR(255);

-- 2. Update bảng ORDER_ITEMS
ALTER TABLE order_items
ALTER COLUMN created_by TYPE VARCHAR(255),
    ALTER COLUMN updated_by TYPE VARCHAR(255),
    ALTER COLUMN deleted_by TYPE VARCHAR(255);

-- ================================================================
-- PHẦN 2: TẠO MỚI CÁC BẢNG CART VÀ CART_ITEMS
-- Lưu ý: Audit log ở đây dùng luôn VARCHAR(255) cho đồng bộ
-- ================================================================

-- 3. Tạo bảng CARTS
CREATE TABLE carts (
                       id BIGINT NOT NULL,
                       user_id BIGINT NOT NULL,

    -- Audit Columns (BaseTsidEntity)
                       created_at TIMESTAMP WITHOUT TIME ZONE,
                       updated_at TIMESTAMP WITHOUT TIME ZONE,
                       created_by VARCHAR(255), -- Đã dùng String theo yêu cầu mới
                       updated_by VARCHAR(255),

                       CONSTRAINT pk_carts PRIMARY KEY (id)
);

-- Constraint: Mỗi User chỉ có 1 Cart
ALTER TABLE carts
    ADD CONSTRAINT uc_carts_user_id UNIQUE (user_id);

-- FK: User (Lưu ý: Bạn check lại tên bảng user của bạn là 'users' hay 'user_profiles' nhé)
-- Dựa theo code cũ bạn cung cấp thì mình để là 'users' cho khớp
ALTER TABLE carts
    ADD CONSTRAINT fk_carts_user_id FOREIGN KEY (user_id) REFERENCES users (id);


-- 4. Tạo bảng CART_ITEMS
CREATE TABLE cart_items (
                            id BIGINT NOT NULL,
                            cart_id BIGINT NOT NULL,
                            product_id BIGINT NOT NULL, -- Chỉ lưu Product, bắt buộc có
                            quantity INTEGER NOT NULL,

    -- Audit Columns
                            created_at TIMESTAMP WITHOUT TIME ZONE,
                            updated_at TIMESTAMP WITHOUT TIME ZONE,
                            created_by VARCHAR(255),
                            updated_by VARCHAR(255),

                            CONSTRAINT pk_cart_items PRIMARY KEY (id)
);

-- Constraint: Trong 1 giỏ hàng, 1 sản phẩm chỉ xuất hiện 1 dòng
ALTER TABLE cart_items
    ADD CONSTRAINT uc_cart_items_product_in_cart UNIQUE (cart_id, product_id);

-- FK: Cart (Cascade Delete: Xóa giỏ -> Xóa item)
ALTER TABLE cart_items
    ADD CONSTRAINT fk_cart_items_cart_id
        FOREIGN KEY (cart_id) REFERENCES carts (id) ON DELETE CASCADE;

-- FK: Product
ALTER TABLE cart_items
    ADD CONSTRAINT fk_cart_items_product_id
        FOREIGN KEY (product_id) REFERENCES products (id);

-- Index cho FK để query nhanh
CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX idx_cart_items_product_id ON cart_items(product_id);