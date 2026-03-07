# Feature 01 — Domain Entities & Enums: Plan

## How

### Step 1: Create Enums
- `enums/Role.java` — Remove `FUNERAL_PARLOUR` for MVP
- `enums/Gender.java` — Remove `UNKNOWN` for MVP
- `enums/DeathCaseStatus.java` — Remove `ESCALATED_OUT_OF_SCOPE` for MVP

### Step 2: Create Core Entities
- `entity/Sector.java` — as proposed
- `entity/User.java` — add `phone`, `email`, `sector` (ManyToOne, nullable), `enabled` (default true), `locked` (default false)
- `entity/Deceased.java` — as proposed, add `address` field

### Step 3: Create Document Hierarchy
- `entity/document/OfficialDocument.java` — abstract, JOINED inheritance
- `entity/document/FormB24.java`
- `entity/document/FormB12.java`
- `entity/document/FormB11.java`
- `entity/document/FormB2.java`

### Step 4: Create Aggregate Root
- `entity/DeathCase.java` — with sector assignment, state machine methods, audit timestamps

### Step 5: Create Repositories
- `repository/UserRepository.java` — `findByNic()`
- `repository/SectorRepository.java` — `findByCode()`
- `repository/DeathCaseRepository.java` — `findByStatus()`, `findByApplicantFamilyMember()`
- `repository/DeceasedRepository.java`

### Files to Create
| Action | Path |
|---|---|
| NEW | `enums/Role.java` |
| NEW | `enums/Gender.java` |
| NEW | `enums/DeathCaseStatus.java` |
| NEW | `entity/Sector.java` |
| NEW | `entity/User.java` |
| NEW | `entity/Deceased.java` |
| NEW | `entity/document/OfficialDocument.java` |
| NEW | `entity/document/FormB24.java` |
| NEW | `entity/document/FormB12.java` |
| NEW | `entity/document/FormB11.java` |
| NEW | `entity/document/FormB2.java` |
| NEW | `entity/DeathCase.java` |
| NEW | `repository/UserRepository.java` |
| NEW | `repository/SectorRepository.java` |
| NEW | `repository/DeathCaseRepository.java` |
| NEW | `repository/DeceasedRepository.java` |

### Dependencies
- **Depends on**: Feature 00 (DB config must be in place)
