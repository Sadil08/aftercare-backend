# Feature 03 — Case Initiation: Plan

## How

### Step 1: DTOs
- **File**: `dto/CreateCaseRequest.java` — validated fields with Jakarta `@NotBlank`, `@NotNull`
- **File**: `dto/CaseResponse.java` — caseId, status, deceased summary, timestamps

### Step 2: Service
- **File**: `service/DeathCaseService.java`
  - `createCase(User applicant, CreateCaseRequest req)`:
    1. Lookup Sector by code
    2. Create Deceased entity
    3. Create DeathCase (constructor enforces CITIZEN role)
    4. Save and return

### Step 3: Controller
- **File**: `controller/DeathCaseController.java`
  - `POST /api/cases` — extracts authenticated user from SecurityContext, calls service

### Files to Create
| Action | Path |
|---|---|
| NEW | `dto/CreateCaseRequest.java` |
| NEW | `dto/CaseResponse.java` |
| NEW | `service/DeathCaseService.java` |
| NEW | `controller/DeathCaseController.java` |

### Dependencies
- **Depends on**: Feature 01 (entities), Feature 02 (auth — need authenticated user)
