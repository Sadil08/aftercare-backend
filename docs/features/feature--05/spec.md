# Feature 05 — Doctor Verification & B-12 Issuance: Specification

## What
Allow a logged-in Doctor to input the ICD-10 medical cause of death and issue Form B-12.

## Why
The B-12 (Certificate of Cause of Death) is legally required before the family can make their declaration. It establishes the medical cause.

## API Endpoint

### `POST /api/cases/{caseId}/b12`
**Role**: `DOCTOR`

**Request Body**:
```json
{
  "icd10Code": "I25.1",
  "primaryCause": "Chronic ischemic heart disease"
}
```

**Response** (`200 OK`):
```json
{
  "caseId": 1,
  "status": "FAMILY_DECLARATION_PENDING",
  "formB12": {
    "documentType": "B-12_MEDICAL_CAUSE",
    "icd10Code": "I25.1",
    "primaryCause": "Chronic ischemic heart disease",
    "issuedAt": "2026-03-07T19:00:00"
  }
}
```

## Acceptance Criteria
- [ ] Doctor issues B-12 → status changes to `FAMILY_DECLARATION_PENDING`
- [ ] Non-doctor → 403
- [ ] Invalid ICD-10 format → 400
- [ ] Case not in `MEDICAL_VERIFICATION_PENDING` → 400
