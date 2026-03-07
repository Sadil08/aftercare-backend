# Feature 01 — Domain Entities & Enums: Specification

## What
Define all core domain objects — enums, value objects, entities, and the document inheritance hierarchy — so they can be persisted and used by all subsequent features.

## Why
The domain model is the heart of the system. Every feature (auth, case creation, document issuance) depends on these classes existing with correct JPA mappings.

## Entities to Create

### Enums
| Enum | Values |
|---|---|
| `Role` | `CITIZEN`, `GN`, `DOCTOR`, `REGISTRAR` |
| `Gender` | `MALE`, `FEMALE`, `OTHER` |
| `DeathCaseStatus` | `DRAFT`, `GN_VERIFICATION_PENDING`, `MEDICAL_VERIFICATION_PENDING`, `FAMILY_DECLARATION_PENDING`, `REGISTRAR_REVIEW`, `B2_ISSUED_CLOSED` |

### Core Entities
| Entity | Key Fields |
|---|---|
| `Sector` | `id`, `code` (unique), `name`, `district` |
| `User` | `id`, `nic` (unique), `fullName`, `passwordHash`, `phone`, `email`, `roles` (Set<Role>), `sector` (nullable FK), `enabled`, `locked` |
| `Deceased` | `id`, `fullName`, `nic`, `dateOfBirth`, `dateOfDeath`, `gender`, `residenceSector` (FK) |

### Document Hierarchy
| Entity | Extends | Specific Fields |
|---|---|---|
| `OfficialDocument` (abstract) | — | `id`, `issuedBy` (FK), `issuedAt`, `cryptographicHash` |
| `FormB24` | `OfficialDocument` | `identityVerified`, `residenceVerified` |
| `FormB12` | `OfficialDocument` | `icd10Code`, `primaryCause` |
| `FormB11` | `OfficialDocument` | `applicantRelationship`, `declarationTrue` |
| `FormB2` | `OfficialDocument` | `certificateSerialNumber` (unique) |

### Aggregate Root
| Entity | Key Fields |
|---|---|
| `DeathCase` | `id`, `applicantFamilyMember` (FK), `deceased` (1:1), `sector` (FK), `status`, `formB24`, `formB12`, `formB11`, `formB2`, `createdAt`, `updatedAt` |

## Acceptance Criteria

- [ ] Application starts and all tables are auto-created in PostgreSQL
- [ ] No `enum` keyword conflicts (package named `enums`)
- [ ] `DeathCase` state machine methods enforce role and status guards
