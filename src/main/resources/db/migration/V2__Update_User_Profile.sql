-- 1. Add columns as NULLable first
ALTER TABLE user_profiles
    ADD COLUMN first_name VARCHAR(50),
    ADD COLUMN last_name VARCHAR(50);

-- 2. Populate data for existing rows to avoid NOT NULL violation
-- Logic: Split full_name by the first space.
-- If full_name = 'Nguyen Van A' -> first_name = 'Nguyen', last_name = 'Van A'
-- If full_name = 'Thang' -> first_name = 'Thang', last_name = 'User' (Placeholder)
UPDATE user_profiles
SET
    first_name = CASE
                     WHEN position(' ' in full_name) > 0 THEN split_part(full_name, ' ', 1)
                     ELSE full_name
        END,
    last_name = CASE
                    WHEN position(' ' in full_name) > 0 THEN substring(full_name from position(' ' in full_name) + 1)
                    ELSE 'User' -- Default placeholder if no last name part found
        END;

-- 3. Apply NOT NULL constraints now that data is populated
ALTER TABLE user_profiles
    ALTER COLUMN first_name SET NOT NULL,
    ALTER COLUMN last_name SET NOT NULL;
