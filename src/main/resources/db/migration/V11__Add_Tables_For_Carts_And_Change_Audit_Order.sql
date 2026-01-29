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
-- PHẦN 2: TẠO MỚI CÁC BẢNG CART VÀ CART_ITEMS (CẬP NHẬT LOGIC TTL + TOKEN)
-- ================================================================

-- 3. Tạo bảng CARTS
CREATE TABLE carts (
                       id BIGINT NOT NULL,

    -- Cho phép NULL để hỗ trợ Guest (Khách vãng lai)
                       user_id BIGINT,

    -- Token định danh giỏ hàng cho Guest (Lưu trong Cookie/LocalStorage)
                       cart_token VARCHAR(255),

    -- TTL: Thời điểm hết hạn của giỏ hàng
                       expires_at TIMESTAMP WITHOUT TIME ZONE,

    -- Audit Columns
                       created_at TIMESTAMP WITHOUT TIME ZONE,
                       updated_at TIMESTAMP WITHOUT TIME ZONE,
                       created_by VARCHAR(255),
                       updated_by VARCHAR(255),

                       CONSTRAINT pk_carts PRIMARY KEY (id)
);

-- Constraint: Đảm bảo tính duy nhất
-- 1. Nếu có user_id, mỗi user chỉ có 1 cart active (tùy logic)
-- CREATE UNIQUE INDEX idx_carts_user_id ON carts(user_id) WHERE status = 'ACTIVE';

-- 2. Cart Token phải là duy nhất
CREATE UNIQUE INDEX idx_carts_cart_token ON carts(cart_token);

-- FK: User (Vẫn giữ liên kết nhưng cột user_id giờ có thể null)
ALTER TABLE carts
    ADD CONSTRAINT fk_carts_user_id FOREIGN KEY (user_id) REFERENCES users (id);


-- 4. Tạo bảng CART_ITEMS (GIỮ NGUYÊN LOGIC LIÊN KẾT ID)
CREATE TABLE cart_items (
                            id BIGINT NOT NULL,
                            cart_id BIGINT NOT NULL, -- Vẫn link bằng ID, không dùng Token ở đây
                            product_id BIGINT NOT NULL,
                            quantity INTEGER NOT NULL,

    -- Audit Columns
                            created_at TIMESTAMP WITHOUT TIME ZONE,
                            updated_at TIMESTAMP WITHOUT TIME ZONE,
                            created_by VARCHAR(255),
                            updated_by VARCHAR(255),

                            CONSTRAINT pk_cart_items PRIMARY KEY (id)
);

-- Constraint: 1 Sản phẩm chỉ xuất hiện 1 lần trong 1 giỏ
ALTER TABLE cart_items
    ADD CONSTRAINT uc_cart_items_product_in_cart UNIQUE (cart_id, product_id);

-- FK: Cart
ALTER TABLE cart_items
    ADD CONSTRAINT fk_cart_items_cart_id
        FOREIGN KEY (cart_id) REFERENCES carts (id) ON DELETE CASCADE;

-- FK: Product
ALTER TABLE cart_items
    ADD CONSTRAINT fk_cart_items_product_id
        FOREIGN KEY (product_id) REFERENCES products (id);

-- Index
CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX idx_cart_items_product_id ON cart_items(product_id);