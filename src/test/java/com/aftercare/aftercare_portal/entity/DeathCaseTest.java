package com.aftercare.aftercare_portal.entity;

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
        assertEquals(DeathCaseStatus.PENDING_B12_MEDICAL, deathCase.getStatus());
        assertEquals(familyMember, deathCase.getApplicantFamilyMember());
        assertEquals(deceased, deathCase.getDeceased());
        assertEquals(sector, deathCase.getSector());
        assertNotNull(deathCase.getCreatedAt());
        assertNotNull(deathCase.getUpdatedAt());
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
    void testFullHappyPathLifecycle() {
        // Phase 2: Doctor Medical Certification
        deathCase.issueB12(doctor, "hash1", true, "I21.9", "Acute myocardial infarction");
        assertEquals(DeathCaseStatus.PENDING_B24_GN, deathCase.getStatus());
        assertNotNull(deathCase.getFormB12());

        // Phase 3: GN Verification
        deathCase.issueB24(gn, "hash2", true, true);
        assertEquals(DeathCaseStatus.PENDING_CR2_FAMILY, deathCase.getStatus());
        assertNotNull(deathCase.getFormB24());

        // Phase 4: Family Declaration
        deathCase.submitCr2Family(familyMember, "hash3", "{\"fullName\":\"John Doe\"}");
        assertEquals(DeathCaseStatus.PENDING_REGISTRAR_REVIEW, deathCase.getStatus());
        assertNotNull(deathCase.getFormCr2FamilyInfo());

        // Phase 5: Registrar CR-2
        deathCase.issueCr2(registrar, "hash4", "CR2-2026-COL-0000001");
        assertEquals(DeathCaseStatus.CR2_ISSUED_CLOSED, deathCase.getStatus());
        assertNotNull(deathCase.getFormCr2());
    }

    @Test
    void testDoctorVerificationFailsWithWrongRole() {
        assertThrows(SecurityException.class, () -> deathCase.issueB12(familyMember, "hash1", true, "I21.9", "Cause"));
    }

    @Test
    void testGNVerificationFailsIfOutOfOrder() {
        // Trying to issue B-24 while still awaiting Doctor B-12
        assertThrows(IllegalStateException.class, () -> deathCase.issueB24(gn, "hash2", true, true));
    }

    @Test
    void testFamilyDeclarationRequiresOriginalApplicant() {
        // Fast-forward to B-11 stage
        deathCase.issueB12(doctor, "hash1", true, "I21.9", "Heart Attack");
        deathCase.issueB24(gn, "hash2", true, true);

        User otherFamily = new User("other1", "other@test.com", "Other Family", "hash", "0711", "1111");
        otherFamily.grantRole(Role.FAMILY);
        setEntityId(otherFamily, 99L);

        assertThrows(SecurityException.class, () -> deathCase.submitCr2Family(otherFamily, "hash3", "{}"));
    }

    @Test
    void testRegistrarFailsIfPredecessorDocumentsMissing() {
        DeathCase brokenCase = new DeathCase(familyMember, deceased, sector);
        assertThrows(IllegalStateException.class, () -> brokenCase.issueCr2(registrar, "hash4", "123"));
    }
}
