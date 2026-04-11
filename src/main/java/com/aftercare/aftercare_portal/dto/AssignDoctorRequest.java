package com.aftercare.aftercare_portal.dto;

import jakarta.validation.constraints.NotBlank;

public record AssignDoctorRequest(
        @NotBlank(message = "Doctor ID is required (e.g. DOC-A1B2C3)") String doctorId) {
}

