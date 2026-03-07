# Feature 04 — GN Verification & B-24 Issuance: Plan

## How

### Step 1: DTO
- **File**: `dto/IssueB24Request.java` — `identityVerified`, `residenceVerified`

### Step 2: Service Method
- Add to `DeathCaseService`:
  - `issueB24(Long caseId, User actingGN, IssueB24Request req)`
  - Computes SHA-256 hash, calls `deathCase.issueB24()`

### Step 3: Hashing Utility
- **File**: `service/HashService.java` — `computeDocumentHash(String... fields)` using SHA-256

### Step 4: Controller Endpoint
- Add to `DeathCaseController`:
  - `POST /api/cases/{caseId}/b24`
  - `GET /api/cases` with query param filtering

### Step 5: Repository Query
- Add to `DeathCaseRepository`:
  - `findByStatusAndDeceased_ResidenceSector(DeathCaseStatus, Sector)`

### Files to Create/Modify
| Action | Path |
|---|---|
| NEW | `dto/IssueB24Request.java` |
| NEW | `service/HashService.java` |
| MODIFY | `service/DeathCaseService.java` |
| MODIFY | `controller/DeathCaseController.java` |
| MODIFY | `repository/DeathCaseRepository.java` |

### Dependencies
- **Depends on**: Feature 03 (case must exist first)
