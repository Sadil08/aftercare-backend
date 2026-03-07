# Feature 08 — Case Tracking & Status Dashboard: Plan

## How

### Step 1: DTOs
- **File**: `dto/CaseListResponse.java` — summary for list views
- Reuse `CaseResponse.java` for detailed view (from Feature 03)

### Step 2: Repository Queries
- Add to `DeathCaseRepository`:
  - Paginated queries by status, sector, applicant
  - `findByDeceased_Nic(String nic)`

### Step 3: Service Methods
- Add to `DeathCaseService`:
  - `getCasesForUser(User user, DeathCaseStatus status, Pageable pageable)`
  - `getCaseDetail(Long caseId, User user)` — with access-control check
  - `searchByDeceasedNic(String nic, User user)`

### Step 4: Controller Endpoints
- Add to `DeathCaseController`:
  - `GET /api/cases` (list)
  - `GET /api/cases/{caseId}` (detail)
  - `GET /api/cases/search` (by deceased NIC)

### Files to Create/Modify
| Action | Path |
|---|---|
| NEW | `dto/CaseListResponse.java` |
| MODIFY | `repository/DeathCaseRepository.java` |
| MODIFY | `service/DeathCaseService.java` |
| MODIFY | `controller/DeathCaseController.java` |

### Dependencies
- **Depends on**: Feature 02 (auth), Feature 03 (cases must exist)
