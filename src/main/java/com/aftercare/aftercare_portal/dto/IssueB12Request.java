package com.aftercare.aftercare_portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.util.List;

public record IssueB12Request(
        @NotNull(message = "Natural death declaration is required") Boolean naturalDeath,

        @NotBlank(message = "ICD-10 code is required") @Pattern(regexp = "^[A-Z][0-9]{2}(\\.[0-9]{1,2})?$", message = "Invalid ICD-10 code format") String icd10Code,

        @NotBlank(message = "Immediate cause of death is required") String immediateCause,

        List<String> antecedentCauses,

        List<String> contributoryCauses,

        @NotNull(message = "Viewed body timestamp is required") LocalDateTime doctorViewedBodyAt,

        @NotBlank(message = "Doctor designation is required") String doctorDesignation,

        @NotBlank(message = "SLMC registration number is required") String slmcRegistrationNo) {
}
