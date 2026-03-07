# Feature 08 — Case Tracking & Status Dashboard: Constitution

## Governing Principles

1. **Transparency**: Every authenticated user can track their own cases (Citizens see cases they initiated; Officials see cases in their domain).
2. **Read-Only**: This feature is purely read-only. No state mutations.
3. **Role-Scoped Views**:
   - `CITIZEN` → cases they initiated
   - `GN` → cases in their sector
   - `DOCTOR` → cases with status `MEDICAL_VERIFICATION_PENDING` or later
   - `REGISTRAR` → all cases in `REGISTRAR_REVIEW` or `B2_ISSUED_CLOSED`

## Non-Functional Constraints

- Paginated responses (default page size: 20).
- Search by case ID or deceased NIC.
