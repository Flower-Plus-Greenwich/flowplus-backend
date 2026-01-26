ALTER TABLE product_categories
    ADD created_at TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE product_categories
    ADD created_by VARCHAR(255);

ALTER TABLE product_categories
    ADD deleted_at TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE product_categories
    ADD id BIGINT;

ALTER TABLE product_categories
    ADD updated_at TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE product_categories
    ADD updated_by VARCHAR(255);

ALTER TABLE product_categories
    ADD CONSTRAINT pk_product_categories PRIMARY KEY (id);

CREATE INDEX idx_product_category_category ON product_categories (category_id);

CREATE INDEX idx_product_category_product ON product_categories (product_id);
