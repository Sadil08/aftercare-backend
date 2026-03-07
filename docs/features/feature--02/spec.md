# Feature 02 — Identity & Authentication: Specification

## What
Implement user registration and login endpoints that issue JWT tokens. Protect all other endpoints with role-based authorization.

## Why
Every actor in the workflow (Family, GN, Doctor, Registrar) must authenticate before performing any action. The system must know **who** is acting and **what role** they hold to enforce the state machine guards.

## Functional Requirements

### Registration (`POST /api/auth/register`)
- **Input**: NIC, fullName, password, phone, role (CITIZEN only via self-registration; officials seeded or admin-created)
- **Output**: User created, returns user ID
- **Rules**: NIC must be unique. Password stored as BCrypt hash.

### Login (`POST /api/auth/login`)
- **Input**: NIC, password
- **Output**: JWT token containing `userId`, `nic`, `roles`, `sectorCode`
- **Rules**: Reject if user is locked or disabled.

### Authorization
- JWT is sent as `Authorization: Bearer <token>` header.
- Spring Security filter validates JWT on every request.
- Endpoint-level role checks:
  | Endpoint Pattern | Allowed Roles |
  |---|---|
  | `POST /api/cases` | `CITIZEN` |
  | `POST /api/cases/*/b24` | `GN` |
  | `POST /api/cases/*/b12` | `DOCTOR` |
  | `POST /api/cases/*/b11` | `CITIZEN` |
  | `POST /api/cases/*/b2` | `REGISTRAR` |
  | `GET /api/cases/**` | All authenticated |

## Acceptance Criteria
- [ ] Register a user, login, receive JWT
- [ ] Accessing a protected endpoint without JWT returns 401
- [ ] Accessing an endpoint with wrong role returns 403
