# Feature 07 — Registrar Review & B-2 Issuance: Specification

## What
Allow a logged-in Registrar to review the complete death case packet, verify document integrity, and issue the final B-2 death certificate.

## Why
The B-2 certificate is the legally binding final output of the entire system. It closes the case and allows the family to proceed with funeral arrangements.

## API Endpoints

### `GET /api/cases?status=REGISTRAR_REVIEW`
**Role**: `REGISTRAR`

### `POST /api/cases/{caseId}/b2`
**Role**: `REGISTRAR`

**Request Body**: (empty — system auto-generates serial number)
```json
{}
```

**Response** (`200 OK`):
```json
{
  "caseId": 1,
  "status": "B2_ISSUED_CLOSED",
  "formB2": {
    "documentType": "B-2_DEATH_CERTIFICATE",
    "certificateSerialNumber": "B2-2026-COL-00001",
    "issuedAt": "2026-03-07T20:00:00"
  }
}
```

## Acceptance Criteria
- [ ] Registrar issues B-2 → status changes to `B2_ISSUED_CLOSED`
- [ ] Attempting B-2 when any predecessor document is missing → 400
- [ ] Non-registrar → 403
- [ ] Re-issuing B-2 on a closed case → 400 "Invalid state transition"
