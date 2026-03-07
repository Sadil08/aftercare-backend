# Feature 02 — Identity & Authentication: Plan

## How

### Step 1: Add JWT Dependency
- Add `io.jsonwebtoken:jjwt-api`, `jjwt-impl`, `jjwt-jackson` to `pom.xml`

### Step 2: JWT Utility
- **File**: `security/JwtUtil.java`
- Methods: `generateToken(User)`, `validateToken(String)`, `extractClaims(String)`

### Step 3: UserDetailsService
- **File**: `security/CustomUserDetailsService.java`
- Loads user by NIC from `UserRepository`

### Step 4: JWT Authentication Filter
- **File**: `security/JwtAuthenticationFilter.java`
- Extends `OncePerRequestFilter`, extracts Bearer token, validates, sets SecurityContext

### Step 5: Security Configuration
- **File**: `config/SecurityConfig.java`
- Disables CSRF (stateless API), configures endpoint-level role restrictions, registers JWT filter

### Step 6: Auth Service & Controller
- **File**: `service/AuthService.java` — register, login logic
- **File**: `controller/AuthController.java` — `POST /api/auth/register`, `POST /api/auth/login`
- **File**: `dto/RegisterRequest.java`, `dto/LoginRequest.java`, `dto/AuthResponse.java`

### Files to Create
| Action | Path |
|---|---|
| MODIFY | `pom.xml` (add JJWT dependency) |
| NEW | `security/JwtUtil.java` |
| NEW | `security/CustomUserDetailsService.java` |
| NEW | `security/JwtAuthenticationFilter.java` |
| NEW | `config/SecurityConfig.java` |
| NEW | `service/AuthService.java` |
| NEW | `controller/AuthController.java` |
| NEW | `dto/RegisterRequest.java` |
| NEW | `dto/LoginRequest.java` |
| NEW | `dto/AuthResponse.java` |

### Dependencies
- **Depends on**: Feature 00 (config), Feature 01 (`User` entity, `UserRepository`)
