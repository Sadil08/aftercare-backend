package com.aftercare.aftercare_portal.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public record CaseResponse(
        Long caseId,
        String status,
        String applicantName,
        String applicantNic,
        String deceasedFullName,
        String deceasedOfficialName,
        String deceasedNic,
        LocalDate dateOfBirth,
        LocalDate dateOfDeath,
        String gender,
        String address,
        String sectorCode,
        String sectorName,
        String assignedDoctorId,
        String assignedDoctorName,
        Map<String, Object> familyReport,
        Map<String, Object> b12HeaderPrefill,
        Map<String, Object> b24Prefill,
        Map<String, Object> cr2Prefill,
        Map<String, Object> formB12,
        Map<String, Object> formB24,
        Map<String, Object> formCr2Family,
        Map<String, Object> formCr2,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
