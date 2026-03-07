package com.aftercare.aftercare_portal.dto;

import java.util.Set;

public record AuthResponse(
        String token,
        Long userId,
        String nic,
        String fullName,
        Set<String> roles) {
}
