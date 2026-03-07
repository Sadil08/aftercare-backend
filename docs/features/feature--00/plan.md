# Feature 00 — Project Foundation: Plan

## How

### 1. Configure `application.properties`
- PostgreSQL datasource URL, username, password
- JPA dialect, DDL-auto=update, show-sql=true (dev)
- Server port (8080)

### 2. Create Package Skeleton
Create empty packages under `com.aftercare.aftercare_portal`:
- `config/`, `enums/`, `entity/`, `entity/document/`, `repository/`, `service/`, `controller/`, `dto/`, `exception/`, `security/`

### 3. Global Exception Handler
- **File**: `exception/GlobalExceptionHandler.java`
- **File**: `exception/ErrorResponse.java` (DTO record)
- Handles: `IllegalArgumentException`, `IllegalStateException`, `SecurityException`, `EntityNotFoundException`, generic `Exception`

### 4. CORS Configuration
- **File**: `config/WebConfig.java`
- Implements `WebMvcConfigurer.addCorsMappings()`

### Dependencies
- None (this is the root feature).

### Files to Create/Modify
| Action | File |
|---|---|
| MODIFY | `src/main/resources/application.properties` |
| NEW | `config/WebConfig.java` |
| NEW | `exception/GlobalExceptionHandler.java` |
| NEW | `exception/ErrorResponse.java` |
