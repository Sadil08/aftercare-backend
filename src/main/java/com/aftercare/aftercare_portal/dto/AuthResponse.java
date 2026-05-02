package com.aftercare.aftercare_portal.dto;

import java.util.Set;

public record AuthResponse(
        String token,
        Long userId,
        String username,
        String email,
        String fullName,
        String nicNo,
        Set<String> roles,
        /** Alphanumeric Doctor ID (e.g. DOC-A1B2C3). Null for non-doctor users. */
        String doctorId,
        /** False immediately after registration — frontend should prompt for OTP verification. */
        boolean phoneVerified) {
}

