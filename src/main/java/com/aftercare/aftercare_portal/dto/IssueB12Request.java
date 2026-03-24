package com.aftercare.aftercare_portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record IssueB12Request(
        @NotNull(message = "Natural death declaration is required") Boolean naturalDeath,

        @NotBlank(message = "ICD-10 code is required") @Pattern(regexp = "^[A-Z][0-9]{2}(\\.[0-9]{1,2})?$", message = "Invalid ICD-10 code format") String icd10Code,

        @NotBlank(message = "Primary cause of death is required") String primaryCause) {
}
