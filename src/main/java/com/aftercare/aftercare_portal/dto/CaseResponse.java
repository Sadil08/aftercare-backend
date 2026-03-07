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

        // Documents (null if not yet issued)
        Map<String, Object> formB24,
        Map<String, Object> formB12,
        Map<String, Object> formB11,
        Map<String, Object> formB2,

        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
