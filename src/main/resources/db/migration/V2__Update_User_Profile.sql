ALTER TABLE user_profiles
    ADD first_name VARCHAR(50);

ALTER TABLE user_profiles
    ADD last_name VARCHAR(50);

ALTER TABLE user_profiles
    ALTER COLUMN first_name SET NOT NULL;

ALTER TABLE user_profiles
    ALTER COLUMN last_name SET NOT NULL;