package com.aftercare.aftercare_portal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingDTO {
    private Long formId;
    private String formType; // e.g., "CR02", "B24"
    private String currentStage; // SUBMITTED_BY_GN, REVIEW_BY_REGISTRAR, APPROVED, READY_FOR_PICKUP
    private LocalDateTime updatedAt;
    private LocalDateTime submittedAt;
    private String deceasedName;
}
