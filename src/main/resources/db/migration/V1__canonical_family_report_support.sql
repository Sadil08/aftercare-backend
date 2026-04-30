ALTER TABLE IF EXISTS deceased ADD COLUMN IF NOT EXISTS full_name_official_language VARCHAR(255);
ALTER TABLE IF EXISTS deceased ADD COLUMN IF NOT EXISTS full_name_english VARCHAR(255);
ALTER TABLE IF EXISTS deceased ADD COLUMN IF NOT EXISTS identification_status VARCHAR(64);
ALTER TABLE IF EXISTS deceased ADD COLUMN IF NOT EXISTS passport_country VARCHAR(255);
ALTER TABLE IF EXISTS deceased ADD COLUMN IF NOT EXISTS passport_number VARCHAR(255);
ALTER TABLE IF EXISTS deceased ADD COLUMN IF NOT EXISTS age_years INTEGER;
ALTER TABLE IF EXISTS deceased ADD COLUMN IF NOT EXISTS age_months INTEGER;
ALTER TABLE IF EXISTS deceased ADD COLUMN IF NOT EXISTS age_days INTEGER;
ALTER TABLE IF EXISTS deceased ADD COLUMN IF NOT EXISTS nationality VARCHAR(255);
ALTER TABLE IF EXISTS deceased ADD COLUMN IF NOT EXISTS race VARCHAR(255);
ALTER TABLE IF EXISTS deceased ADD COLUMN IF NOT EXISTS profession VARCHAR(255);
ALTER TABLE IF EXISTS deceased ADD COLUMN IF NOT EXISTS pension_status BOOLEAN;
ALTER TABLE IF EXISTS deceased ADD COLUMN IF NOT EXISTS father_nic VARCHAR(255);
ALTER TABLE IF EXISTS deceased ADD COLUMN IF NOT EXISTS father_name VARCHAR(255);
ALTER TABLE IF EXISTS deceased ADD COLUMN IF NOT EXISTS mother_nic VARCHAR(255);
ALTER TABLE IF EXISTS deceased ADD COLUMN IF NOT EXISTS mother_name VARCHAR(255);
ALTER TABLE IF EXISTS deceased ADD COLUMN IF NOT EXISTS permanent_address_full_text VARCHAR(255);
ALTER TABLE IF EXISTS deceased ADD COLUMN IF NOT EXISTS permanent_district VARCHAR(255);
ALTER TABLE IF EXISTS deceased ADD COLUMN IF NOT EXISTS permanent_ds_division VARCHAR(255);
ALTER TABLE IF EXISTS deceased ADD COLUMN IF NOT EXISTS permanent_gn_division VARCHAR(255);
ALTER TABLE IF EXISTS deceased ADD COLUMN IF NOT EXISTS maternal_was_pregnant_at_death BOOLEAN;
ALTER TABLE IF EXISTS deceased ADD COLUMN IF NOT EXISTS maternal_gave_birth_within42_days BOOLEAN;
ALTER TABLE IF EXISTS deceased ADD COLUMN IF NOT EXISTS maternal_had_abortion BOOLEAN;
ALTER TABLE IF EXISTS deceased ADD COLUMN IF NOT EXISTS maternal_days_since_birth_or_abortion INTEGER;

ALTER TABLE IF EXISTS form_cr2_family_info ADD COLUMN IF NOT EXISTS family_report_json TEXT;

ALTER TABLE IF EXISTS form_b12 ADD COLUMN IF NOT EXISTS antecedent_causes_json TEXT;
ALTER TABLE IF EXISTS form_b12 ADD COLUMN IF NOT EXISTS contributory_causes_json TEXT;
ALTER TABLE IF EXISTS form_b12 ADD COLUMN IF NOT EXISTS doctor_viewed_body_at TIMESTAMP;
ALTER TABLE IF EXISTS form_b12 ADD COLUMN IF NOT EXISTS doctor_designation VARCHAR(255);
ALTER TABLE IF EXISTS form_b12 ADD COLUMN IF NOT EXISTS slmc_registration_no VARCHAR(255);

CREATE TABLE IF NOT EXISTS form_b24 (
    id BIGINT PRIMARY KEY,
    grama_division VARCHAR(255),
    registrar_division VARCHAR(255),
    serial_no VARCHAR(255),
    death_date DATE,
    place_of_death VARCHAR(255),
    full_name VARCHAR(255),
    sex VARCHAR(255),
    race VARCHAR(255),
    age VARCHAR(255),
    profession VARCHAR(255),
    cause_of_death TEXT,
    informant_name VARCHAR(255),
    informant_address TEXT,
    registrar_name VARCHAR(255),
    signed_at VARCHAR(255),
    sign_date DATE,
    gn_signature VARCHAR(255),
    confirmed BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE IF EXISTS form_b24
    ADD CONSTRAINT IF NOT EXISTS fk_form_b24_official_document
    FOREIGN KEY (id) REFERENCES official_documents (id);

ALTER TABLE IF EXISTS death_cases ADD COLUMN IF NOT EXISTS form_b24_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_death_cases_form_b24'
    ) THEN
        ALTER TABLE death_cases
            ADD CONSTRAINT fk_death_cases_form_b24
            FOREIGN KEY (form_b24_id) REFERENCES form_b24 (id);
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS ux_death_cases_form_b24_id ON death_cases (form_b24_id);
