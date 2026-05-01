package com.aftercare.aftercare_portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Username is required") String username,

        @NotBlank(message = "Email is required") String email,

        @NotBlank(message = "Full name is required") String fullName,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,}$",
                message = "Password must contain at least one uppercase letter, one digit, and one special character"
        )
        String password,

        String phone,
        String nicNo,

        String role,

        String sectorCode) {
}
