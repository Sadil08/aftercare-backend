# Feature 06 — Family Declaration & B-11 Submission: Specification

## What
Allow the original applicant (Family/Citizen) to review the case data and submit their sworn declaration (Form B-11).

## Why
The B-11 is the family's legal statement. It completes the document set required for the Registrar's final review.

## API Endpoint

### `POST /api/cases/{caseId}/b11`
**Role**: `CITIZEN` (must be the original applicant)

**Request Body**:
```json
{
  "relationship": "Son",
  "declarationTrue": true
}
```

**Response** (`200 OK`):
```json
{
  "caseId": 1,
  "status": "REGISTRAR_REVIEW",
  "formB11": {
    "documentType": "B-11_FAMILY_APPLICATION",
    "applicantRelationship": "Son",
    "declarationTrue": true,
    "issuedAt": "2026-03-07T19:30:00"
  }
}
```

## Acceptance Criteria
- [ ] Original applicant submits B-11 → status changes to `REGISTRAR_REVIEW`
- [ ] Different citizen → 403
- [ ] Case not in `FAMILY_DECLARATION_PENDING` → 400
