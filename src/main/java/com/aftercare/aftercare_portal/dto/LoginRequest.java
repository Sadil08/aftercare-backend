package com.aftercare.aftercare_portal.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "NIC is required") String nic,

        @NotBlank(message = "Password is required") String password) {
}
