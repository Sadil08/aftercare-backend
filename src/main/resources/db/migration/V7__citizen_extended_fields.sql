-- Extend citizens table with fields needed for death form pre-fill
ALTER TABLE citizens
    ADD COLUMN IF NOT EXISTS full_name_sinhala   VARCHAR(255),
    ADD COLUMN IF NOT EXISTS nationality         VARCHAR(100),
    ADD COLUMN IF NOT EXISTS ethnicity           VARCHAR(100),
    ADD COLUMN IF NOT EXISTS address_district    VARCHAR(100),
    ADD COLUMN IF NOT EXISTS address_division    VARCHAR(100),
    ADD COLUMN IF NOT EXISTS address_gn_division VARCHAR(100),
    ADD COLUMN IF NOT EXISTS occupation          VARCHAR(150),
    ADD COLUMN IF NOT EXISTS marital_status      VARCHAR(50);
