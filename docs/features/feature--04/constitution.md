# Feature 04 — GN Verification & B-24 Issuance: Constitution

## Governing Principles

1. **Sector-Scoped Access**: A GN can only view/act on cases within their assigned sector.
2. **Sequential State Gate**: B-24 can only be issued when case status is `GN_VERIFICATION_PENDING`.
3. **Single Authority**: Only users with `GN` role can issue Form B-24.
4. **Immutable After Signing**: Once the B-24 is issued, its hash is locked. No edits allowed.

## Non-Functional Constraints

- The hash is computed as `SHA-256(caseId + deceasedNic + gnUserId + timestamp)` for MVP (simplified).
