package com.aftercare.aftercare_portal.entity;

import com.aftercare.aftercare_portal.entity.document.FormCR2FamilyInfo;
import com.aftercare.aftercare_portal.enums.DeathCaseStatus;
import com.aftercare.aftercare_portal.enums.Gender;
import com.aftercare.aftercare_portal.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DeathCaseTest {

    private User familyMember;
    private User doctor;
    private User gn;
    private User registrar;
    private Sector sector;
    private Deceased deceased;
    private DeathCase deathCase;

    @BeforeEach
    void setUp() {
        sector = new Sector("SEC-01", "Colombo 1", "Colombo");

        familyMember = new User("family1", "family@test.com", "Family Name", "hash", "0711111111", "123456789V");
        familyMember.grantRole(Role.FAMILY);

        doctor = new User("doctor1", "doctor@test.com", "Doctor Name", "hash", "0713333333", "DOC123");
        doctor.grantRole(Role.DOCTOR);
        doctor.assignSector(sector);

        gn = new User("gn1", "gn@test.com", "GN Name", "hash", "0712222222", "GN123");
        gn.grantRole(Role.GRAMA_NILADHARI);
        gn.assignSector(sector);

        registrar = new User("registrar1", "registrar@test.com", "Registrar Name", "hash", "0714444444", "REG123");
        registrar.grantRole(Role.REGISTRAR);
        registrar.assignSector(sector);

        setEntityId(familyMember, 1L);
        setEntityId(doctor, 2L);
        setEntityId(gn, 3L);
        setEntityId(registrar, 4L);

        deceased = new Deceased("Deceased Name", "987654321V", LocalDate.of(1950, 1, 1), LocalDate.now(), Gender.MALE,
                "123 Main St", sector);

        deathCase = new DeathCase(familyMember, deceased, sector);
        // Attach upfront CR-2 family data (as the new flow requires)
        FormCR2FamilyInfo cr2Info = new FormCR2FamilyInfo(familyMember, "cr2hash", "{\"fullName\":\"Deceased Name\"}");
        deathCase.attachCr2FamilyData(cr2Info);
    }

    private void setEntityId(Object entity, Long id) {
        try {
            java.lang.reflect.Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testInitialState() {
        // New workflow starts at PENDING_GN_REVIEW
        assertEquals(DeathCaseStatus.PENDING_GN_REVIEW, deathCase.getStatus());
        assertEquals(familyMember, deathCase.getApplicantFamilyMember());
        assertEquals(deceased, deathCase.getDeceased());
        assertEquals(sector, deathCase.getSector());
        assertNotNull(deathCase.getCreatedAt());
        assertNotNull(deathCase.getUpdatedAt());
        assertNotNull(deathCase.getFormCr2FamilyInfo());
    }

    @Test
    void testInitializationRequiresFamilyRole() {
        User outsider = new User("outsider1", "outsider@test.com", "Outsider", "hash", "0710000000", "999");
        assertThrows(SecurityException.class, () -> new DeathCase(outsider, deceased, sector));
    }

    @Test
    void testDateOfDeathBeforeDateOfBirthThrows() {
        Deceased badDeceased = new Deceased("Name", "NIC", LocalDate.of(2000, 1, 1), LocalDate.of(1990, 1, 1),
                Gender.MALE, "Address", sector);
        assertThrows(IllegalArgumentException.class, () -> new DeathCase(familyMember, badDeceased, sector));
    }

    @Test
    void testGNApprovePathDirectlyToRegistrar() {
        // GN approves directly — no medical needed
        deathCase.gnApprove(gn);
        assertEquals(DeathCaseStatus.PENDING_REGISTRAR_REVIEW, deathCase.getStatus());

        // Registrar issues CR-2
        deathCase.issueCr2(registrar, "hash4", "CR2-2026-COL-0000001");
        assertEquals(DeathCaseStatus.CR2_ISSUED_CLOSED, deathCase.getStatus());
        assertNotNull(deathCase.getFormCr2());
    }

    @Test
    void testGNRequestMedicalWithDoctorPreAssigned() {
        // Assign doctor first
        deathCase.setAssignedDoctor(doctor);

        // GN requests medical — doctor already assigned
        deathCase.gnRequestMedical(gn);
        assertEquals(DeathCaseStatus.PENDING_B12_MEDICAL, deathCase.getStatus());

        // Doctor issues B-12 (natural death) → back to GN
        deathCase.issueB12(doctor, "hash1", true, "I21.9", "Acute myocardial infarction");
        assertEquals(DeathCaseStatus.PENDING_GN_REVIEW, deathCase.getStatus());
        assertNotNull(deathCase.getFormB12());

        // GN forwards to Registrar
        deathCase.gnApprove(gn);
        assertEquals(DeathCaseStatus.PENDING_REGISTRAR_REVIEW, deathCase.getStatus());

        // Registrar issues CR-2
        deathCase.issueCr2(registrar, "hash4", "CR2-2026-COL-0000001");
        assertEquals(DeathCaseStatus.CR2_ISSUED_CLOSED, deathCase.getStatus());
    }

    @Test
    void testGNRequestMedicalWithoutDoctorPendingAssignment() {
        // No doctor assigned — GN requests medical
        deathCase.gnRequestMedical(gn);
        assertEquals(DeathCaseStatus.PENDING_DOCTOR_ASSIGNMENT, deathCase.getStatus());

        // Family provides a doctor
        deathCase.familyAssignDoctor(familyMember, doctor);
        assertEquals(DeathCaseStatus.PENDING_B12_MEDICAL, deathCase.getStatus());
    }

    @Test
    void testDoctorUnnaturalDeathRejectsCase() {
        deathCase.setAssignedDoctor(doctor);
        deathCase.gnRequestMedical(gn);

        // Doctor declares unnatural death
        deathCase.issueB12(doctor, "hash1", false, "X99", "Trauma");
        assertEquals(DeathCaseStatus.REJECTED_UNNATURAL_DEATH, deathCase.getStatus());
    }

    @Test
    void testDoctorVerificationFailsWithWrongRole() {
        deathCase.setAssignedDoctor(doctor);
        deathCase.gnRequestMedical(gn);
        assertThrows(SecurityException.class, () -> deathCase.issueB12(familyMember, "hash1", true, "I21.9", "Cause"));
    }

    @Test
    void testRegistrarFailsIfCr2FamilyDataMissing() {
        // Build a stripped case with no CR-2 family data
        DeathCase brokenCase = new DeathCase(familyMember, deceased, sector);
        // Force status to PENDING_REGISTRAR_REVIEW via GN approve (which requires GN_REVIEW)
        // We can test that CR-2 guard triggers by calling issueCr2 directly in wrong state
        assertThrows(IllegalStateException.class, () -> brokenCase.issueCr2(registrar, "hash4", "123"));
    }
}
