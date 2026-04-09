package com.aftercare.aftercare_portal.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public record CaseResponse(
        Long caseId,
        String status,

        // Applicant info
        String applicantName,
        String applicantNic,

        // Deceased info
        String deceasedFullName,
        String deceasedNic,
        LocalDate dateOfDeath,
        String gender,
        String address,
        String sectorCode,
        String sectorName,

        // Assigned doctor (may be null)
        String assignedDoctorName,

        // Documents in flow order (null if not yet applicable)
        Map<String, Object> formB12,
        Map<String, Object> formCr2Family,
        Map<String, Object> formCr2,

        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}

