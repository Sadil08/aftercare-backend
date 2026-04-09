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

    /** The doctor explicitly routed to this case (set at creation or assigned later by family). */
    @ManyToOne
    @JoinColumn(name = "assigned_doctor_id")
    private User assignedDoctor;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "form_b12_id")
    private FormB12 formB12;

    /**
     * CR-2 family declaration data — now collected at case creation time.
     * Null only for legacy / test cases.
     */
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
        this.status = DeathCaseStatus.PENDING_GN_REVIEW;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    // ──── Data attachment (called after construction in the service layer) ────

    /** Attaches the family CR-2 declaration data supplied at case creation. */
    public void attachCr2FamilyData(FormCR2FamilyInfo info) {
        this.formCr2FamilyInfo = info;
        this.updatedAt = LocalDateTime.now();
    }

    /** Sets the optional pre-assigned doctor supplied at case creation. */
    public void setAssignedDoctor(User doctor) {
        requireRole(doctor, Role.DOCTOR, "Assigned user must be a Doctor.");
        this.assignedDoctor = doctor;
        this.updatedAt = LocalDateTime.now();
    }

    // ──── State Machine Methods ────

    /**
     * GN Action — Approve: forwards case directly to Registrar.
     * Valid from PENDING_GN_REVIEW (initial or post-B12 natural death).
     */
    public void gnApprove(User actingGN) {
        requireStatus(DeathCaseStatus.PENDING_GN_REVIEW);
        requireRole(actingGN, Role.GRAMA_NILADHARI, "Only a Grama Niladhari can approve a case.");
        requireSameSector(actingGN);
        this.status = DeathCaseStatus.PENDING_REGISTRAR_REVIEW;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * GN Action — Request Medical Confirmation.
     * Routes to PENDING_B12_MEDICAL if a doctor is assigned, or PENDING_DOCTOR_ASSIGNMENT
     * if the family has not yet provided a doctor.
     */
    public void gnRequestMedical(User actingGN) {
        requireStatus(DeathCaseStatus.PENDING_GN_REVIEW);
        requireRole(actingGN, Role.GRAMA_NILADHARI, "Only a Grama Niladhari can request medical confirmation.");
        requireSameSector(actingGN);
        if (this.assignedDoctor != null) {
            this.status = DeathCaseStatus.PENDING_B12_MEDICAL;
        } else {
            this.status = DeathCaseStatus.PENDING_DOCTOR_ASSIGNMENT;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Family assigns a doctor after being prompted (PENDING_DOCTOR_ASSIGNMENT).
     */
    public void familyAssignDoctor(User actingFamily, User doctor) {
        requireStatus(DeathCaseStatus.PENDING_DOCTOR_ASSIGNMENT);
        if (!actingFamily.getId().equals(this.applicantFamilyMember.getId())) {
            throw new SecurityException("Only the original applicant can assign a doctor.");
        }
        requireRole(doctor, Role.DOCTOR, "The specified user must be a Doctor.");
        this.assignedDoctor = doctor;
        this.status = DeathCaseStatus.PENDING_B12_MEDICAL;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Doctor issues B-12 (Medical Certification).
     * — Natural death → returns case to GN for final forwarding (PENDING_GN_REVIEW)
     * — Unnatural death → REJECTED_UNNATURAL_DEATH
     */
    public void issueB12(User actingDoctor, String signatureHash,
                         boolean naturalDeath, String icd10Code, String primaryCause) {
        requireStatus(DeathCaseStatus.PENDING_B12_MEDICAL);
        requireRole(actingDoctor, Role.DOCTOR, "Only a Doctor can issue a B-12.");
        // Enforce that only the assigned doctor may act (if one is set)
        if (this.assignedDoctor != null && !actingDoctor.getId().equals(this.assignedDoctor.getId())) {
            throw new SecurityException("Only the assigned Doctor can issue a B-12 for this case.");
        }

        this.formB12 = new FormB12(actingDoctor, signatureHash, naturalDeath, icd10Code, primaryCause);

        if (!naturalDeath) {
            this.status = DeathCaseStatus.REJECTED_UNNATURAL_DEATH;
        } else {
            // Return to GN so they can forward to Registrar
            this.status = DeathCaseStatus.PENDING_GN_REVIEW;
        }

        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Registrar issues CR-2 (Death Certificate).
     * CR-2 family data must have been provided at case creation.
     */
    public void issueCr2(User actingRegistrar, String signatureHash, String serialNumber) {
        requireStatus(DeathCaseStatus.PENDING_REGISTRAR_REVIEW);
        requireRole(actingRegistrar, Role.REGISTRAR, "Only a Registrar can issue a CR-2.");

        if (this.formCr2FamilyInfo == null) {
            throw new IllegalStateException("Cannot issue CR-2: missing family CR-2 declaration data.");
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

    private void requireSameSector(User gnUser) {
        if (gnUser.getSector() == null || !gnUser.getSector().getId().equals(this.sector.getId())) {
            throw new SecurityException("You can only act on cases in your assigned sector.");
        }
    }
}
