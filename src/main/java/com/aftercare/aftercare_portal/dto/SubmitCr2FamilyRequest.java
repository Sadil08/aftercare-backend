package com.aftercare.aftercare_portal.dto;

import jakarta.validation.constraints.NotBlank;

public record SubmitCr2FamilyRequest(
        @NotBlank(message = "CR-2 form data payload is required") String cr2FormData) {
}
