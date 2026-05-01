package com.aftercare.aftercare_portal.entity;

import com.aftercare.aftercare_portal.enums.DeathCaseStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "case_audit_log")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaseAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "death_case_id")
    private DeathCase deathCase;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String performedByUsername;

    @Enumerated(EnumType.STRING)
    private DeathCaseStatus fromStatus;

    @Enumerated(EnumType.STRING)
    private DeathCaseStatus toStatus;

    @Column(nullable = false)
    private LocalDateTime performedAt;

    private String ipAddress;
}
