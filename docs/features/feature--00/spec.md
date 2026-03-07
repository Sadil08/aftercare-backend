# Feature 00 — Project Foundation: Specification

## What
Set up the project's runtime configuration, database connectivity, global error handling, and package skeleton so all subsequent features have a stable base to build on.

## Why
Without a configured database, CORS policy, and structured error responses, no feature can function. This is the prerequisite for everything.

## Functional Requirements

1. **Database Connection**: Application connects to a PostgreSQL database using properties from `application.properties`.
2. **Package Skeleton**: All top-level packages exist (even if empty) so developers know where to place code.
3. **Global Exception Handler**: A `@RestControllerAdvice` catches common exceptions and returns JSON:
   ```json
   { "timestamp": "...", "status": 400, "error": "Bad Request", "message": "..." }
   ```
4. **CORS Configuration**: Allows requests from `localhost:3000` and `localhost:5173`.
5. **Health Check**: The default Spring Actuator `/actuator/health` endpoint is available (optional for MVP).

## Acceptance Criteria

- [ ] `./mvnw spring-boot:run` starts without errors when a PostgreSQL DB is available.
- [ ] A deliberate bad request returns a structured JSON error, not a Spring whitelabel page.
- [ ] CORS preflight from `localhost:3000` succeeds.
