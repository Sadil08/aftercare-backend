# Feature 02 — Identity & Authentication: Tasks

- [ ] Add JJWT dependencies to `pom.xml`
- [ ] Create `JwtUtil` (generate, validate, extract)
- [ ] Create `CustomUserDetailsService`
- [ ] Create `JwtAuthenticationFilter`
- [ ] Create `SecurityConfig` with endpoint-role mappings
- [ ] Create `RegisterRequest`, `LoginRequest`, `AuthResponse` DTOs
- [ ] Create `AuthService` (register + login)
- [ ] Create `AuthController` (register + login endpoints)
- [ ] Verify: register → login → access protected endpoint
- [ ] Verify: wrong role → 403, no token → 401
