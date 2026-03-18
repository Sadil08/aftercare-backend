package com.aftercare.aftercare_portal.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "cemetery_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CemeteryRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String familyNicNo;

    @Column(nullable = false)
    private Long cr02FormId;

    private String deceasedName;

    @Column(nullable = false)
    private String cemeteryUsername;

    @Column(nullable = false)
    private String requestedDate; // e.g., "2026-03-20"

    @Column(nullable = false)
    private String status; // PENDING, APPROVED, REJECTED

    private String timeSlot; // e.g., "14:00 - 16:00"

    @CreationTimestamp
    private LocalDateTime createdAt;
}
