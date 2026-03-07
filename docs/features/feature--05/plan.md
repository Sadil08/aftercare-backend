# Feature 05 — Doctor Verification & B-12 Issuance: Plan

## How

### Step 1: DTO
- **File**: `dto/IssueB12Request.java` — `icd10Code` (with `@Pattern` regex), `primaryCause`

### Step 2: Service Method
- Add to `DeathCaseService`:
  - `issueB12(Long caseId, User actingDoctor, IssueB12Request req)`

### Step 3: Controller Endpoint
- Add to `DeathCaseController`:
  - `POST /api/cases/{caseId}/b12`

### Files to Create/Modify
| Action | Path |
|---|---|
| NEW | `dto/IssueB12Request.java` |
| MODIFY | `service/DeathCaseService.java` |
| MODIFY | `controller/DeathCaseController.java` |

### Dependencies
- **Depends on**: Feature 04 (B-24 must be issued first)
