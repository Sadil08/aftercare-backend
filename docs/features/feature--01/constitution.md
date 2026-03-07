# Feature 01 — Domain Entities & Enums: Constitution

## Governing Principles

1. **Immutable by Default**: Entities use `@Getter` (Lombok) only. Setter access is through controlled business methods, never through public setters.
2. **Protected No-Arg Constructors**: JPA requirement. All entities have `protected` no-arg constructors. Public constructors enforce required invariants.
3. **Enum Safety**: Java enums are persisted as `STRING` (never `ORDINAL`) to prevent silent data corruption on reorder.
4. **Inheritance Strategy**: The `OfficialDocument` hierarchy uses `InheritanceType.JOINED` to normalize storage and avoid wide sparse tables.
5. **Aggregate Boundaries**: `DeathCase` is the aggregate root. Documents are owned by the case (cascade `ALL`). External entities reference the case by ID, not by holding the aggregate.

## Non-Functional Constraints

- No business logic in entities beyond invariant enforcement (e.g., role checks in `DeathCase` constructor).
- Entities must compile and tables must auto-generate cleanly with `ddl-auto=update`.
