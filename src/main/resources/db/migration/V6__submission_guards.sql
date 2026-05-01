-- DB-level unique constraint on deceased NIC (allows NULLs for unidentified/foreign deceased)
CREATE UNIQUE INDEX IF NOT EXISTS idx_deceased_nic_unique ON deceased(nic) WHERE nic IS NOT NULL;

-- OTP fields for phone verification
ALTER TABLE users ADD COLUMN IF NOT EXISTS pending_otp      VARCHAR(6);
ALTER TABLE users ADD COLUMN IF NOT EXISTS otp_expires_at   TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone_verified   BOOLEAN NOT NULL DEFAULT FALSE;
