package com.aftercare.aftercare_portal.dto;

import java.time.LocalDateTime;

public record CaseListResponse(
        Long caseId,
        String status,
        String deceasedFullName,
        String deceasedNic,
        String applicantFullName,
        String causeOfDeath,
        String b12DoctorName,
        String b12DoctorId,
        String b12Icd10Code,
        String sectorName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
