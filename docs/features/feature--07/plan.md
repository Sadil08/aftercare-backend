# Feature 07 — Registrar Review & B-2 Issuance: Plan

## How

### Step 1: Serial Number Generator
- Add to `DeathCaseService` or create `service/CertificateService.java`:
  - `generateSerialNumber(Sector sector)` — queries DB for next sequence in district

### Step 2: Service Method
- Add to `DeathCaseService`:
  - `issueB2(Long caseId, User actingRegistrar)`
  - Internal hash re-verification logic before issuance

### Step 3: Controller Endpoint
- Add to `DeathCaseController`:
  - `POST /api/cases/{caseId}/b2`

### Files to Create/Modify
| Action | Path |
|---|---|
| MODIFY | `service/DeathCaseService.java` |
| MODIFY | `controller/DeathCaseController.java` |

### Dependencies
- **Depends on**: Feature 06 (B-11 must be submitted first)
