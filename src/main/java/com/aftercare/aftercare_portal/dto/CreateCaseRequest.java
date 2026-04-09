package com.aftercare.aftercare_portal.dto;

import com.aftercare.aftercare_portal.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateCaseRequest(
        @NotBlank(message = "Deceased full name is required") String deceasedFullName,

        String deceasedNic,

        LocalDate dateOfBirth,

        @NotNull(message = "Date of death is required") LocalDate dateOfDeath,

        @NotNull(message = "Gender is required") Gender gender,

        @NotBlank(message = "Sector code is required") String sectorCode,

        @NotBlank(message = "Address is required") String address,

        /** Full CR-2 form fields as a JSON string, collected upfront from the family. */
        @NotBlank(message = "CR-2 form data is required") String cr2FormData,

        /** Optional ID of a Doctor to pre-assign for medical confirmation. */
        Long doctorId) {
}
