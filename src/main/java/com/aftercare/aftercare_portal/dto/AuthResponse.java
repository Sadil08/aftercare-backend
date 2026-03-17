package com.aftercare.aftercare_portal.dto;

import java.util.Set;

public record AuthResponse(
        String token,
        Long userId,
        String username,
        String email,
        String fullName,
        String nicNo,
        Set<String> roles) {
}
