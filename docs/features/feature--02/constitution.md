# Feature 02 — Identity & Authentication: Constitution

## Governing Principles

1. **Stateless Authentication**: Use JWT tokens. No server-side sessions.
2. **Passwords Never in Plain Text**: BCrypt hashing for all passwords.
3. **Role-Based Access Control**: Every endpoint is protected by role. No endpoint is unprotected except `/api/auth/register` and `/api/auth/login`.
4. **Principle of Least Privilege**: Users only see data relevant to their role and assigned sector.

## Non-Functional Constraints

- JWT expiry: 24 hours for MVP (shorter in production).
- Failed login attempts: logged but no account lock in MVP (deferred to post-MVP fraud detection).
- OTP/MFA: out of scope for MVP.
