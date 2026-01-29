ALTER TABLE material_stocks
    ADD cost_price DECIMAL(15, 2);

ALTER TABLE material_stocks
    ADD version BIGINT;

ALTER TABLE material_stocks
    ALTER COLUMN cost_price SET NOT NULL;

ALTER TABLE material_stocks
    DROP COLUMN opening_balance;

ALTER TABLE inventory_transactions
    ADD after_quantity INTEGER;

ALTER TABLE inventory_transactions
    ADD after_reserved INTEGER;

ALTER TABLE inventory_transactions
    ADD before_quantity INTEGER;

ALTER TABLE inventory_transactions
    ADD before_reserved INTEGER;

ALTER TABLE inventory_transactions
    ADD cost_price DECIMAL(15, 2);

ALTER TABLE inventory_transactions
    ADD current_balance INTEGER;

ALTER TABLE inventory_transactions
    ADD note TEXT;

ALTER TABLE inventory_transactions
    ADD quantity_delta INTEGER;

ALTER TABLE inventory_transactions
    ADD version BIGINT;

ALTER TABLE inventory_transactions
    ALTER COLUMN after_quantity SET NOT NULL;

ALTER TABLE inventory_transactions
    ALTER COLUMN before_quantity SET NOT NULL;

ALTER TABLE inventory_transactions
    ALTER COLUMN cost_price SET NOT NULL;

ALTER TABLE inventory_transactions
    ALTER COLUMN current_balance SET NOT NULL;

ALTER TABLE inventory_transactions
    ALTER COLUMN quantity_delta SET NOT NULL;

ALTER TABLE categories
    ADD version BIGINT;

ALTER TABLE materials
    ADD version BIGINT;

ALTER TABLE product_categories
    ADD version BIGINT;

ALTER TABLE product_recipes
    ADD version BIGINT;

ALTER TABLE products
    ADD version BIGINT;

ALTER TABLE inventory_transactions
    DROP COLUMN change_amount;

