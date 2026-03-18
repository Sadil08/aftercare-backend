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
    @JoinColumn(name = "form_b12_id")
    private FormB12 formB12;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "form_b24_id")
    private FormB24 formB24;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "form_cr2_family_id")
    private FormCR2FamilyInfo formCr2FamilyInfo;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "form_cr2_id")
    private FormCR2 formCr2;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // ──── Constructor ────

    public DeathCase(User applicant, Deceased deceased, Sector sector) {
        if (!applicant.getRoles().contains(Role.FAMILY)) {
            throw new SecurityException("Initiator must have FAMILY role.");
        }

        // Validate Date of Death >= Date of Birth
        if (deceased.getDateOfBirth() != null && deceased.getDateOfDeath() != null
                && deceased.getDateOfDeath().isBefore(deceased.getDateOfBirth())) {
            throw new IllegalArgumentException("Date of death cannot be before date of birth.");
        }

        this.applicantFamilyMember = applicant;
        this.deceased = deceased;
        this.sector = sector;
        this.status = DeathCaseStatus.PENDING_B12_MEDICAL;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    // ──── State Machine Methods (order matches diagrams) ────

    // Step 2: Doctor issues B-12 (Medical Certification)
    public void issueB12(User actingDoctor, String signatureHash,
                         boolean naturalDeath, String icd10Code, String primaryCause) {
        requireStatus(DeathCaseStatus.PENDING_B12_MEDICAL);
        requireRole(actingDoctor, Role.DOCTOR, "Only a Doctor can issue a B-12.");

        this.formB12 = new FormB12(actingDoctor, signatureHash, naturalDeath, icd10Code, primaryCause);
        
        if (!naturalDeath) {
            this.status = DeathCaseStatus.REJECTED_UNNATURAL_DEATH;
        } else {
            this.status = DeathCaseStatus.PENDING_B24_GN;
        }
        
        this.updatedAt = LocalDateTime.now();
    }

    // Step 3: GN issues B-24 (Identity & Residence Verification)
    public void issueB24(User actingGN, String signatureHash,
                         boolean identityVerified, boolean residenceVerified) {
        requireStatus(DeathCaseStatus.PENDING_B24_GN);
        requireRole(actingGN, Role.GRAMA_NILADHARI, "Only a Grama Niladhari can issue a B-24.");

        this.formB24 = new FormB24(actingGN, signatureHash, identityVerified, residenceVerified);
        this.status = DeathCaseStatus.PENDING_CR2_FAMILY;
        this.updatedAt = LocalDateTime.now();
    }

    // Step 4: Family submits CR-2 Data (Declaration)
    public void submitCr2Family(User actingFamilyMember, String signatureHash, String cr2FormData) {
        requireStatus(DeathCaseStatus.PENDING_CR2_FAMILY);
        if (!actingFamilyMember.getId().equals(this.applicantFamilyMember.getId())) {
            throw new SecurityException("Only the original applicant can submit the CR-2 Declaration.");
        }

        this.formCr2FamilyInfo = new FormCR2FamilyInfo(actingFamilyMember, signatureHash, cr2FormData);
        this.status = DeathCaseStatus.PENDING_REGISTRAR_REVIEW;
        this.updatedAt = LocalDateTime.now();
    }

    // Step 5: Registrar issues CR-2 (Death Certificate)
    public void issueCr2(User actingRegistrar, String signatureHash, String serialNumber) {
        requireStatus(DeathCaseStatus.PENDING_REGISTRAR_REVIEW);
        requireRole(actingRegistrar, Role.REGISTRAR, "Only a Registrar can issue a CR-2.");

        if (this.formB12 == null || this.formB24 == null) {
            throw new IllegalStateException("Cannot issue CR-2: missing predecessor documents (B-12 or B-24).");
        }

        // Registrar must verify identity was confirmed by GN
        if (!this.formB24.isIdentityVerified()) {
            throw new IllegalStateException("Cannot issue CR-2: identity was not verified by GN.");
        }

        this.formCr2 = new FormCR2(actingRegistrar, signatureHash, serialNumber);
        this.status = DeathCaseStatus.CR2_ISSUED_CLOSED;
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
