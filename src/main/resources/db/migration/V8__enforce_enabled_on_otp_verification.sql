-- Disable accounts that registered but never verified their phone.
-- Seeded official accounts have phone_verified = true so they are unaffected.
UPDATE users SET enabled = false WHERE phone_verified = false;
