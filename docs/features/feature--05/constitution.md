# Feature 05 — Doctor Verification & B-12 Issuance: Constitution

## Governing Principles

1. **Only Doctors Issue B-12**: Only users with `DOCTOR` role can input the medical cause and issue Form B-12.
2. **Sequential Gate**: B-12 can only be issued when case status is `MEDICAL_VERIFICATION_PENDING` (B-24 must exist).
3. **ICD-10 Compliance**: The `icd10Code` field must accept valid ICD-10 format codes (validated at DTO level).
4. **Medical Data Sensitivity**: B-12 data contains cause-of-death details — treat as sensitive.

## Non-Functional Constraints

- ICD-10 validation in MVP is format-only (regex `^[A-Z][0-9]{2}(\\.[0-9]{1,2})?$`). Full code-list validation deferred.
