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
public class CemeteryRequestDto {
    private Long id;
    private String familyNicNo;
    private Long cr02FormId;
    private String deceasedName;
    private String cemeteryUsername;
    private String requestedDate;
    private String status;
    private String timeSlot;
    private LocalDateTime createdAt;
}
