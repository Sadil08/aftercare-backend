package com.aftercare.aftercare_portal.dto;

import jakarta.validation.constraints.NotBlank;

public record SubmitB11Request(
        @NotBlank(message = "Relationship to deceased is required") String relationship,

        boolean declarationTrue) {
}
