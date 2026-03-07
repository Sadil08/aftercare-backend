# Feature 06 — Family Declaration & B-11 Submission: Constitution

## Governing Principles

1. **Original Applicant Only**: Only the user who initiated the case can submit the B-11 declaration.
2. **Sequential Gate**: B-11 can only be submitted when status is `FAMILY_DECLARATION_PENDING` (B-24 and B-12 must exist).
3. **Plain Language**: The family interface (future frontend) must present the declaration in plain language, not legal jargon.
4. **Truth Binding**: The `declarationTrue` flag is a digital equivalent of a sworn statement.

## Non-Functional Constraints

- The applicant ID check is enforced in the `DeathCase.submitB11()` domain method, not just at the controller level.
