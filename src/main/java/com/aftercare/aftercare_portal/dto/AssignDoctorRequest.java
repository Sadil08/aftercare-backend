package com.aftercare.aftercare_portal.dto;

import jakarta.validation.constraints.NotNull;

public record AssignDoctorRequest(
        @NotNull(message = "Doctor ID is required") Long doctorId) {
}
