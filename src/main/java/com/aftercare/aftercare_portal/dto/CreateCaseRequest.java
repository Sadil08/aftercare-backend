package com.aftercare.aftercare_portal.dto;

import com.aftercare.aftercare_portal.enums.Gender;
import jakarta.validation.Valid;

import java.time.LocalDate;

public record CreateCaseRequest(
        @Valid CanonicalFamilyReport familyReport,

        // Legacy fields kept temporarily for backward compatibility.
        String deceasedFullName,
        String deceasedNic,
        LocalDate dateOfBirth,
        LocalDate dateOfDeath,
        Gender gender,
        String sectorCode,
        String address,
        String cr2FormData,
        String doctorId,
        String submissionOtp) {
}
