package com.aftercare.aftercare_portal.dto;

import java.time.LocalDateTime;

public record CaseListResponse(
        Long caseId,
        String status,
        String deceasedFullName,
        String deceasedNic,
        String applicantFullName,
        String causeOfDeath,
        String sectorName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
