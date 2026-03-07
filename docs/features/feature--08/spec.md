# Feature 08 — Case Tracking & Status Dashboard: Specification

## What
Provide read-only endpoints for all users to search for and view the current status of death cases relevant to them.

## Why
Transparency is a core value of the system. Families should never need to physically visit an office to ask "where is my case?" — they can see the real-time status digitally.

## API Endpoints

### `GET /api/cases`
**Role**: All authenticated users — results filtered by role
**Query Params**: `status` (optional), `page`, `size`

### `GET /api/cases/{caseId}`
**Role**: All authenticated users — access control checks:
- Citizen must be the applicant
- GN must be in the case's sector
- Doctor/Registrar can view once the case reaches their stage

**Response** (`200 OK` — detailed view):
```json
{
  "caseId": 1,
  "status": "MEDICAL_VERIFICATION_PENDING",
  "applicant": { "fullName": "Sunil Perera", "nic": "198812345678" },
  "deceased": { "fullName": "Kamala Perera", "dateOfDeath": "2026-03-01" },
  "formB24": { "issuedAt": "...", "issuedBy": "..." },
  "formB12": null,
  "formB11": null,
  "formB2": null,
  "createdAt": "2026-03-07T18:00:00"
}
```

### `GET /api/cases/search?deceasedNic={nic}`
**Role**: All authenticated (same access rules as above)

## Acceptance Criteria
- [ ] Citizen sees only their own cases
- [ ] GN sees only their sector's cases
- [ ] Single case detail endpoint returns full document status
- [ ] Pagination works
