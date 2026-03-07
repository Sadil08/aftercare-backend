# Feature 03 — Case Initiation: Specification

## What
Allow a logged-in Family/Citizen user to create a new death case by providing the deceased's details. The case enters `GN_VERIFICATION_PENDING` status.

## Why
This is the entry point of the entire workflow. Without it, no subsequent verification or document issuance can occur.

## API Endpoint

### `POST /api/cases`
**Role**: `CITIZEN`

**Request Body**:
```json
{
  "deceasedFullName": "Kamala Perera",
  "deceasedNic": "199012345678",
  "dateOfBirth": "1990-03-15",
  "dateOfDeath": "2026-03-01",
  "gender": "FEMALE",
  "sectorCode": "GN-KOL-042",
  "address": "123 Temple Road, Colombo 05"
}
```

**Response** (`201 Created`):
```json
{
  "caseId": 1,
  "status": "GN_VERIFICATION_PENDING",
  "createdAt": "2026-03-07T18:00:00"
}
```

## Acceptance Criteria
- [ ] Citizen creates a case → gets 201 with caseId
- [ ] Non-citizen attempts → gets 403
- [ ] Missing required fields → gets 400 with validation errors
- [ ] Duplicate deceased NIC → gets 409 Conflict
