# Feature 07 — Registrar Review & B-2 Issuance: Constitution

## Governing Principles

1. **Final Authority**: Only users with `REGISTRAR` role can issue the B-2 certificate.
2. **Complete Document Set Required**: B-2 cannot be issued unless B-24, B-12, and B-11 all exist with valid hashes.
3. **Idempotency**: The system must prevent duplicate B-2 issuance for the same case. Once `B2_ISSUED_CLOSED`, no further state transitions.
4. **Certificate Serial Number**: Each B-2 gets a unique, system-generated serial number.
5. **Tamper Check**: Before issuing B-2, the Registrar (or the system on their behalf) must verify the cryptographic hashes of all predecessor documents.

## Non-Functional Constraints

- Serial number format: `B2-{YEAR}-{DISTRICT_CODE}-{SEQ}` (e.g., `B2-2026-COL-00001`)
- Hash re-verification is a service-layer check, not manual.
