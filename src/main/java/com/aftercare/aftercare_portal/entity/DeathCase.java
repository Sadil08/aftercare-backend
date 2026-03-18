package com.aftercare.aftercare_portal.entity;

import com.aftercare.aftercare_portal.entity.document.*;
import com.aftercare.aftercare_portal.enums.DeathCaseStatus;
import com.aftercare.aftercare_portal.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.time.LocalDateTime;

@Entity
@Table(name = "death_cases")
@Getter 
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeathCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "applicant_user_id")
    private User applicantFamilyMember;

    @OneToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "deceased_id")
    private Deceased deceased;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sector_id")
    private Sector sector;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeathCaseStatus status;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "form_b24_id")
    private FormB24 formB24;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "form_b12_id")
    private FormB12 formB12;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "form_b11_id")
    private FormB11 formB11;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "form_b2_id")
    private FormB2 formB2;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // ──── Constructor ────

    public DeathCase(User applicant, Deceased deceased, Sector sector) {
        if (!applicant.getRoles().contains(Role.CITIZEN)) {
            throw new SecurityException("Initiator must have CITIZEN role.");
        }
        this.applicantFamilyMember = applicant;
        this.deceased = deceased;
        this.sector = sector;
        this.status = DeathCaseStatus.GN_VERIFICATION_PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    // ──── State Machine Methods ────

    public void issueB24(User actingGN, String signatureHash, boolean identityVerified, boolean residenceVerified) {
        requireStatus(DeathCaseStatus.GN_VERIFICATION_PENDING);
        requireRole(actingGN, Role.GN, "Only a GN can issue a B-24.");

        this.formB24 = new FormB24(actingGN, signatureHash, identityVerified, residenceVerified);
        this.status = DeathCaseStatus.MEDICAL_VERIFICATION_PENDING;
        this.updatedAt = LocalDateTime.now();
    }

    public void issueB12(User actingDoctor, String signatureHash, String icd10Code, String primaryCause) {
        requireStatus(DeathCaseStatus.MEDICAL_VERIFICATION_PENDING);
        requireRole(actingDoctor, Role.DOCTOR, "Only a Doctor can issue a B-12.");

        this.formB12 = new FormB12(actingDoctor, signatureHash, icd10Code, primaryCause);
        this.status = DeathCaseStatus.FAMILY_DECLARATION_PENDING;
        this.updatedAt = LocalDateTime.now();
    }

    public void submitB11(User actingCitizen, String signatureHash, String relationship) {
        requireStatus(DeathCaseStatus.FAMILY_DECLARATION_PENDING);
        if (!actingCitizen.getId().equals(this.applicantFamilyMember.getId())) {
            throw new SecurityException("Only the original applicant can submit the B-11.");
        }

        this.formB11 = new FormB11(actingCitizen, signatureHash, relationship, true);
        this.status = DeathCaseStatus.REGISTRAR_REVIEW;
        this.updatedAt = LocalDateTime.now();
    }

    public void issueB2(User actingRegistrar, String signatureHash, String serialNumber) {
        requireStatus(DeathCaseStatus.REGISTRAR_REVIEW);
        requireRole(actingRegistrar, Role.REGISTRAR, "Only a Registrar can issue a B-2.");

        if (this.formB24 == null || this.formB12 == null || this.formB11 == null) {
            throw new IllegalStateException("Cannot issue B-2: missing predecessor documents.");
        }

        this.formB2 = new FormB2(actingRegistrar, signatureHash, serialNumber);
        this.status = DeathCaseStatus.B2_ISSUED_CLOSED;
        this.updatedAt = LocalDateTime.now();
    }

    // ──── Private Guards ────

    private void requireStatus(DeathCaseStatus expected) {
        if (this.status != expected) {
            throw new IllegalStateException(
                    "Invalid state transition. Current: " + this.status + ", Required: " + expected);
        }
    }

    private void requireRole(User user, Role required, String message) {
        if (!user.getRoles().contains(required)) {
            throw new SecurityException("Unauthorized: " + message);
        }
    }
}
