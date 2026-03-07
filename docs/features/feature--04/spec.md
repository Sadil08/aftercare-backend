# Feature 04 — GN Verification & B-24 Issuance: Specification

## What
Allow a logged-in Grama Niladhari to see pending cases in their sector, verify the deceased's identity and residence, and issue Form B-24.

## Why
The B-24 (Notice of Death) is the first official document in the chain. Without it, the Doctor cannot proceed to issue the medical cause of death (B-12).

## API Endpoints

### `GET /api/cases?status=GN_VERIFICATION_PENDING`
**Role**: `GN` — filtered to their assigned sector

### `POST /api/cases/{caseId}/b24`
**Role**: `GN`

**Request Body**:
```json
{
  "identityVerified": true,
  "residenceVerified": true
}
```

**Response** (`200 OK`):
```json
{
  "caseId": 1,
  "status": "MEDICAL_VERIFICATION_PENDING",
  "formB24": {
    "documentType": "B-24_HOME_REPORT",
    "issuedBy": "GN Wickrama",
    "issuedAt": "2026-03-07T18:30:00",
    "identityVerified": true,
    "residenceVerified": true
  }
}
```

## Acceptance Criteria
- [ ] GN sees only cases in their sector
- [ ] GN issues B-24 → status changes to `MEDICAL_VERIFICATION_PENDING`
- [ ] Non-GN → 403
- [ ] Case not in correct status → 400 with "Invalid state transition"
