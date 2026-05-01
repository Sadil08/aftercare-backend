package com.aftercare.aftercare_portal.entity;

import com.aftercare.aftercare_portal.entity.document.FormB12;
import com.aftercare.aftercare_portal.entity.document.FormB24;
import com.aftercare.aftercare_portal.entity.document.FormCR2;
import com.aftercare.aftercare_portal.entity.document.FormCR2FamilyInfo;
import com.aftercare.aftercare_portal.enums.DeathCaseStatus;
import com.aftercare.aftercare_portal.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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

    @ManyToOne
    @JoinColumn(name = "assigned_doctor_id")
    private User assignedDoctor;

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

    public DeathCase(User applicant, Deceased deceased, Sector sector) {
        if (!applicant.getRoles().contains(Role.FAMILY)) {
            throw new SecurityException("Initiator must have FAMILY role.");
        }

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

    public void attachCr2FamilyData(FormCR2FamilyInfo info) {
        this.formCr2FamilyInfo = info;
        this.updatedAt = LocalDateTime.now();
    }

    public void setAssignedDoctor(User doctor) {
        requireRole(doctor, Role.DOCTOR, "Assigned user must be a Doctor.");
        this.assignedDoctor = doctor;
        this.updatedAt = LocalDateTime.now();
    }

    public void gnApprove(User actingGN) {
        requireStatus(DeathCaseStatus.PENDING_GN_REVIEW);
        requireRole(actingGN, Role.GRAMA_NILADHARI, "Only a Grama Niladhari can approve a case.");
        requireSameSector(actingGN);
        requireNoConflictOfInterest(actingGN);
        this.status = DeathCaseStatus.PENDING_REGISTRAR_REVIEW;
        this.updatedAt = LocalDateTime.now();
    }

    public void gnRequestMedical(User actingGN) {
        requireStatus(DeathCaseStatus.PENDING_GN_REVIEW);
        requireRole(actingGN, Role.GRAMA_NILADHARI, "Only a Grama Niladhari can request medical confirmation.");
        requireSameSector(actingGN);
        requireNoConflictOfInterest(actingGN);
        if (this.assignedDoctor != null) {
            this.status = DeathCaseStatus.PENDING_B12_MEDICAL;
        } else {
            this.status = DeathCaseStatus.PENDING_DOCTOR_ASSIGNMENT;
        }
        this.updatedAt = LocalDateTime.now();
    }

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

    public void issueB12(User actingDoctor, String signatureHash,
            boolean naturalDeath, String icd10Code, String immediateCause,
            String antecedentCausesJson, String contributoryCausesJson,
            LocalDateTime doctorViewedBodyAt, String doctorDesignation, String slmcRegistrationNo) {
        requireStatus(DeathCaseStatus.PENDING_B12_MEDICAL);
        requireRole(actingDoctor, Role.DOCTOR, "Only a Doctor can issue a B-12.");
        if (this.assignedDoctor != null && !actingDoctor.getId().equals(this.assignedDoctor.getId())) {
            throw new SecurityException("Only the assigned Doctor can issue a B-12 for this case.");
        }

        this.formB12 = new FormB12(
                actingDoctor,
                signatureHash,
                naturalDeath,
                icd10Code,
                immediateCause,
                antecedentCausesJson,
                contributoryCausesJson,
                doctorViewedBodyAt,
                doctorDesignation,
                slmcRegistrationNo);

        this.status = naturalDeath ? DeathCaseStatus.PENDING_GN_REVIEW : DeathCaseStatus.REJECTED_UNNATURAL_DEATH;
        this.updatedAt = LocalDateTime.now();
    }

    public void issueB12(User actingDoctor, String signatureHash,
            boolean naturalDeath, String icd10Code, String primaryCause) {
        issueB12(
                actingDoctor,
                signatureHash,
                naturalDeath,
                icd10Code,
                primaryCause,
                null,
                null,
                null,
                null,
                null);
    }

    public void issueB24(User actingGN, String signatureHash, String gramaDivision, String registrarDivision,
            String serialNo, LocalDate deathDate, String placeOfDeath, String fullName, String sex, String race,
            String age, String profession, String causeOfDeath, String informantName, String informantAddress,
            String registrarName, String signedAt, LocalDate signDate, String gnSignature, boolean confirmed) {
        requireStatus(DeathCaseStatus.PENDING_GN_REVIEW);
        requireRole(actingGN, Role.GRAMA_NILADHARI, "Only a Grama Niladhari can issue a B-24.");
        requireSameSector(actingGN);

        this.formB24 = new FormB24(
                actingGN,
                signatureHash,
                gramaDivision,
                registrarDivision,
                serialNo,
                deathDate,
                placeOfDeath,
                fullName,
                sex,
                race,
                age,
                profession,
                causeOfDeath,
                informantName,
                informantAddress,
                registrarName,
                signedAt,
                signDate,
                gnSignature,
                confirmed);
        this.status = DeathCaseStatus.PENDING_REGISTRAR_REVIEW;
        this.updatedAt = LocalDateTime.now();
    }

    public void issueCr2(User actingRegistrar, String signatureHash, String serialNumber) {
        requireStatus(DeathCaseStatus.PENDING_REGISTRAR_REVIEW);
        requireRole(actingRegistrar, Role.REGISTRAR, "Only a Registrar can issue a CR-2.");

        if (this.formCr2FamilyInfo == null) {
            throw new IllegalStateException("Cannot issue CR-2: missing family declaration data.");
        }

        this.formCr2 = new FormCR2(actingRegistrar, signatureHash, serialNumber);
        this.status = DeathCaseStatus.CR2_ISSUED_CLOSED;
        this.updatedAt = LocalDateTime.now();
    }

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

    private void requireNoConflictOfInterest(User actingGN) {
        if (actingGN.getNicNo() != null
                && actingGN.getNicNo().equals(this.applicantFamilyMember.getNicNo())) {
            throw new SecurityException(
                    "Conflict of interest: a GN cannot review a case they submitted as a family member.");
        }
        if (actingGN.getNicNo() != null
                && actingGN.getNicNo().equals(this.deceased.getNic())) {
            throw new SecurityException(
                    "Conflict of interest: a GN cannot review a case involving their own death.");
        }
    }
}
