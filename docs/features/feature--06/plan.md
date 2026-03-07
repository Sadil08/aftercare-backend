# Feature 06 — Family Declaration & B-11 Submission: Plan

## How

### Step 1: DTO
- **File**: `dto/SubmitB11Request.java` — `relationship`, `declarationTrue`

### Step 2: Service Method
- Add to `DeathCaseService`:
  - `submitB11(Long caseId, User actingCitizen, SubmitB11Request req)`

### Step 3: Controller Endpoint
- Add to `DeathCaseController`:
  - `POST /api/cases/{caseId}/b11`

### Files to Create/Modify
| Action | Path |
|---|---|
| NEW | `dto/SubmitB11Request.java` |
| MODIFY | `service/DeathCaseService.java` |
| MODIFY | `controller/DeathCaseController.java` |

### Dependencies
- **Depends on**: Feature 05 (B-12 must be issued first)
