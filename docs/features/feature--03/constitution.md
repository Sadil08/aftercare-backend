# Feature 03 — Case Initiation: Constitution

## Governing Principles

1. **Only Citizens Initiate**: Only users with the `CITIZEN` role can create a death case.
2. **One Case Per Deceased**: The system must prevent duplicate cases for the same deceased NIC.
3. **Immediate Routing**: Upon creation, case status is `GN_VERIFICATION_PENDING` and should be visible to GNs in the relevant sector.
4. **Data Integrity**: The deceased's sector determines which GN(s) are notified.

## Non-Functional Constraints

- Case creation must be idempotent — resubmitting the same request must not create duplicates.
- All case data is validated at the DTO level before reaching the service layer.
