package com.aftercare.aftercare_portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "NIC is required") String nic,

        @NotBlank(message = "Full name is required") String fullName,

        @NotBlank(message = "Password is required") @Size(min = 6, message = "Password must be at least 6 characters") String password,

        String phone,
        String email,

        String role,

        String sectorCode) {
}
