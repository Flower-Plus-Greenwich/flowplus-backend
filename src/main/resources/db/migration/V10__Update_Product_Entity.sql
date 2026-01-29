ALTER TABLE products
    ADD is_seasonal_priority BOOLEAN;

ALTER TABLE products
    ADD premake_instruction VARCHAR(255);

ALTER TABLE products
    DROP COLUMN prepared_quantity;

