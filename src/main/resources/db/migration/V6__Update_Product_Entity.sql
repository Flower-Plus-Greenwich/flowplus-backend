ALTER TABLE products
    ADD care_instruction TEXT;

ALTER TABLE products
    ADD cost_price DECIMAL(15, 2);

ALTER TABLE products
    ADD height_cm INTEGER;

ALTER TABLE products
    ADD length_cm INTEGER;

ALTER TABLE products
    ADD original_price DECIMAL(15, 2);

ALTER TABLE products RENAME base_price
    TO selling_price;

ALTER TABLE products
    ADD weight_g INTEGER;

ALTER TABLE products
    ADD width_cm INTEGER;

ALTER TABLE products
    ALTER COLUMN cost_price SET NOT NULL;

ALTER TABLE products
    ALTER COLUMN original_price SET NOT NULL;

ALTER TABLE products
    ALTER COLUMN selling_price SET NOT NULL;

