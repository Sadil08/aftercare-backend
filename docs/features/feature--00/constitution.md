# Feature 00 — Project Foundation: Constitution

## Governing Principles

1. **Convention over Configuration**: Follow Spring Boot conventions. Minimal custom config.
2. **Fail Fast**: Application must refuse to start if the database is unreachable.
3. **Environment Separation**: All secrets (DB password, JWT secret) come from environment variables or `application.properties`, never hardcoded.
4. **Clean Package Structure**: Enforce the layered package convention (`config/`, `enums/`, `entity/`, `repository/`, `service/`, `controller/`, `dto/`, `exception/`, `security/`).

## Non-Functional Constraints

- PostgreSQL as the single persistence store.
- JPA auto-DDL (`spring.jpa.hibernate.ddl-auto=update`) for MVP — migrations (Flyway/Liquibase) deferred to post-MVP.
- CORS must allow `http://localhost:3000` (future frontend) and `http://localhost:5173` (Vite dev).
- Global exception handler returns structured JSON error responses.
