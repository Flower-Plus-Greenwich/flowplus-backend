CREATE TABLE orders (
    -- Các cột từ BaseTsidSoftDeleteEntity
                        id BIGINT PRIMARY KEY,
                        created_at TIMESTAMP,
                        updated_at TIMESTAMP,
                        deleted_at TIMESTAMP,
                        created_by BIGINT,
                        updated_by BIGINT,
                        deleted_by BIGINT,

    -- Các cột riêng của Order
                        user_id BIGINT NOT NULL,
                        total_amount DECIMAL(15, 2) NOT NULL,
                        discount_amount DECIMAL(15, 2),
                        status VARCHAR(30) NOT NULL,
                        delivery_status VARCHAR(30),
                        payment_status VARCHAR(20),
                        payment_method VARCHAR(30),

    -- Cột JSONB cho địa chỉ
                        shipping_address JSONB NOT NULL,

                        delivery_date TIMESTAMP,
                        delivery_time_slot VARCHAR(50),
                        customer_note TEXT,

                        paid_at TIMESTAMP,
                        completed_at TIMESTAMP,
                        cancelled_at TIMESTAMP,

                        order_code VARCHAR(20) NOT NULL,

    -- Ràng buộc Unique cho order_code
                        CONSTRAINT uq_orders_order_code UNIQUE (order_code)
);

CREATE INDEX idx_order_user_id ON orders(user_id);


CREATE TABLE order_items (
    -- Các cột từ BaseTsidSoftDeleteEntity
                             id BIGINT PRIMARY KEY,
                             created_at TIMESTAMP,
                             updated_at TIMESTAMP,
                             deleted_at TIMESTAMP,
                             created_by BIGINT,
                             updated_by BIGINT,
                             deleted_by BIGINT,

    -- Các cột riêng của OrderItem
                             order_id BIGINT NOT NULL,
                             product_id BIGINT, -- Có thể NULL nếu là Custom
                             item_type VARCHAR(20) NOT NULL,

                             quantity INTEGER NOT NULL,
                             unit_price DECIMAL(15, 2) NOT NULL,
                             unit_cost DECIMAL(15, 2),
                             sub_total DECIMAL(15, 2) NOT NULL,

    -- Cột JSONB cho cấu hình Custom
                             custom_config JSONB
);

CREATE INDEX idx_order_item_order_id ON order_items(order_id);

ALTER TABLE order_items
    ADD CONSTRAINT chk_item_type_logic
        CHECK (
            (item_type = 'PRODUCT' AND product_id IS NOT NULL)
                OR
            (item_type = 'CUSTOM' AND product_id IS NULL AND custom_config IS NOT NULL)
            );


-- Liên kết orders -> users(id)
ALTER TABLE orders
    ADD CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id) REFERENCES users(id);

-- Liên kết order_items -> orders(id)
ALTER TABLE order_items
    ADD CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders(id);

-- Liên kết order_items -> products(id)
ALTER TABLE order_items
    ADD CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id) REFERENCES products(id);