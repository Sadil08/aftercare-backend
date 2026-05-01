package com.aftercare.aftercare_portal.service;

import com.aftercare.aftercare_portal.dto.CanonicalFamilyReport;
import com.aftercare.aftercare_portal.dto.CaseResponse;
import com.aftercare.aftercare_portal.dto.CreateCaseRequest;
import com.aftercare.aftercare_portal.dto.GnActionRequest;
import com.aftercare.aftercare_portal.dto.IssueB12Request;
import com.aftercare.aftercare_portal.dto.IssueB24Request;
import com.aftercare.aftercare_portal.entity.DeathCase;
import com.aftercare.aftercare_portal.entity.Sector;
import com.aftercare.aftercare_portal.entity.User;
import com.aftercare.aftercare_portal.enums.Gender;
import com.aftercare.aftercare_portal.enums.Role;
import com.aftercare.aftercare_portal.repository.CaseAuditLogRepository;
import com.aftercare.aftercare_portal.repository.DeathCaseRepository;
import com.aftercare.aftercare_portal.repository.DeceasedRepository;
import com.aftercare.aftercare_portal.repository.SectorRepository;
import com.aftercare.aftercare_portal.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeathCaseServiceTest {

    @Mock
    private DeathCaseRepository deathCaseRepository;

    @Mock
    private DeceasedRepository deceasedRepository;

    @Mock
    private SectorRepository sectorRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HashService hashService;

    @Mock
    private CaseAuditLogRepository auditLogRepository;

    private DeathCaseService deathCaseService;
    private final AtomicReference<DeathCase> storedCase = new AtomicReference<>();

    private Sector sector;
    private User family;
    private User gn;
    private User doctor;
    private User registrar;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        deathCaseService = new DeathCaseService(
                deathCaseRepository,
                deceasedRepository,
                sectorRepository,
                userRepository,
                hashService,
                objectMapper,
                validator,
                auditLogRepository);

        sector = new Sector("KANDY-01", "Kandy Central", "Kandy");
        family = new User("family1", "family@test.com", "Family Member", "hash", "0711111111", "901234567V");
        family.grantRole(Role.FAMILY);

        gn = new User("gn1", "gn@test.com", "GN Officer", "hash", "0712222222", "801234567V");
        gn.grantRole(Role.GRAMA_NILADHARI);
        gn.assignSector(sector);

        doctor = new User("doctor1", "doctor@test.com", "Doctor Officer", "hash", "0713333333", "701234567V");
        doctor.grantRole(Role.DOCTOR);
        doctor.assignSector(sector);

        registrar = new User("registrar1", "registrar@test.com", "Registrar Officer", "hash", "0714444444", "601234567V");
        registrar.grantRole(Role.REGISTRAR);
        registrar.assignSector(sector);

        setEntityId(sector, 10L);
        setEntityId(family, 1L);
        setEntityId(gn, 2L);
        setEntityId(doctor, 3L);
        setEntityId(registrar, 4L);

        lenient().when(hashService.computeHash(anyString())).thenReturn("hash");
        lenient().when(sectorRepository.findByCode("KANDY-01")).thenReturn(Optional.of(sector));
        lenient().when(deceasedRepository.existsByNic(anyString())).thenReturn(false);
        lenient().when(userRepository.findByDoctorId(doctor.getDoctorId())).thenReturn(Optional.of(doctor));
        lenient().when(deathCaseRepository.save(any(DeathCase.class))).thenAnswer(invocation -> {
            DeathCase deathCase = invocation.getArgument(0);
            if (deathCase.getId() == null) {
                setEntityId(deathCase, 100L);
            }
            storedCase.set(deathCase);
            return deathCase;
        });
        lenient().when(deathCaseRepository.findById(anyLong())).thenAnswer(invocation -> {
            DeathCase deathCase = storedCase.get();
            return deathCase != null ? Optional.of(deathCase) : Optional.empty();
        });
        lenient().when(deathCaseRepository.findFirstByApplicantFamilyMember_NicNoOrderByCreatedAtDesc(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(storedCase.get()));
        lenient().when(deathCaseRepository.count()).thenReturn(5L);
    }

    @Test
    void createCaseValidatesCanonicalModel() {
        CanonicalFamilyReport invalidReport = canonicalReport(null, null, null);
        CreateCaseRequest request = new CreateCaseRequest(invalidReport, null, null, null, null, null, null, null, null, null);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> deathCaseService.createCase(family, request));

        assertTrue(error.getMessage().contains("Provide dateOfBirth or ageYears"));
    }

    @Test
    void doctorFallbackPathMovesToPendingDoctorAssignment() {
        CreateCaseRequest request = requestFor(canonicalReport(null, LocalDate.of(1950, 2, 14), null));
        CaseResponse created = deathCaseService.createCase(family, request);

        assertEquals("PENDING_GN_REVIEW", created.status());
        assertNull(created.assignedDoctorId());

        CaseResponse gnResponse = deathCaseService.gnAction(100L, gn, new GnActionRequest("REQUEST_MEDICAL"));

        assertEquals("PENDING_DOCTOR_ASSIGNMENT", gnResponse.status());
    }

    @Test
    void doctorPreassignedPathMovesToPendingB12Medical() {
        CreateCaseRequest request = requestFor(canonicalReport(doctor.getDoctorId(), LocalDate.of(1950, 2, 14), null));
        CaseResponse created = deathCaseService.createCase(family, request);

        assertEquals(doctor.getDoctorId(), created.assignedDoctorId());

        CaseResponse gnResponse = deathCaseService.gnAction(100L, gn, new GnActionRequest("REQUEST_MEDICAL"));

        assertEquals("PENDING_B12_MEDICAL", gnResponse.status());
        assertEquals(doctor.getDoctorId(), gnResponse.assignedDoctorId());
    }

    @Test
    void b24PersistenceTransitionsCaseToRegistrarReview() {
        deathCaseService.createCase(family, requestFor(canonicalReport(null, LocalDate.of(1950, 2, 14), null)));

        CaseResponse response = deathCaseService.issueB24(100L, gn, new IssueB24Request(
                "Kandy Central",
                "Kandy Registration Division",
                null,
                LocalDate.of(2026, 4, 20),
                "22 Lake Road, Kandy",
                "Sunil Perera",
                "male",
                "Sinhalese",
                "76y 2m 6d",
                "Retired Teacher",
                "Family-reported stroke",
                "Anula Perera",
                "88 Informant Avenue, Kandy",
                null,
                "Kandy Central",
                LocalDate.of(2026, 4, 21),
                "GN Officer",
                true));

        assertEquals("PENDING_REGISTRAR_REVIEW", response.status());
        assertNotNull(storedCase.get().getFormB24());
        assertEquals("22 Lake Road, Kandy", response.formB24().get("b24PlaceOfDeath"));
    }

    @Test
    void getCaseDetailIncludesDateOfBirthAndStructuredFamilyData() {
        deathCaseService.createCase(family, requestFor(canonicalReport(null, LocalDate.of(1950, 2, 14), null)));

        CaseResponse detail = deathCaseService.getCaseDetail(100L, family);

        assertEquals(LocalDate.of(1950, 2, 14), detail.dateOfBirth());
        @SuppressWarnings("unchecked")
        Map<String, Object> deceased = (Map<String, Object>) detail.familyReport().get("deceased");
        assertEquals("Sunil Perera", deceased.get("fullNameEnglish"));
        assertEquals("14 Permanent Home, Kandy", ((Map<?, ?>) deceased.get("permanentAddress")).get("fullText"));
    }

    @Test
    void registrarPrefillUsesMappedFieldsInsteadOfGenericAddressShortcuts() {
        deathCaseService.createCase(family, requestFor(canonicalReport(doctor.getDoctorId(), LocalDate.of(1950, 2, 14), null)));
        deathCaseService.gnAction(100L, gn, new GnActionRequest("REQUEST_MEDICAL"));
        deathCaseService.issueB12(100L, doctor, new IssueB12Request(
                true,
                "I21.9",
                "Myocardial infarction",
                List.of("Coronary artery disease"),
                List.of("Hypertension"),
                LocalDateTime.of(2026, 4, 20, 9, 30),
                "Medical Officer",
                "SLMC-12345"));

        CaseResponse detail = deathCaseService.getCaseDetail(100L, registrar);

        assertEquals(LocalDate.of(1950, 2, 14), detail.dateOfBirth());
        assertEquals("Myocardial infarction", detail.formB12().get("immediateCause"));
        assertEquals("Myocardial infarction", detail.cr2Prefill().get("causeOfDeath"));
        assertEquals("14 Permanent Home, Kandy", detail.cr2Prefill().get("permAddressFullText"));
        assertEquals("22 Lake Road, Kandy", detail.cr2Prefill().get("placeInEnglish"));
        assertEquals("88 Informant Avenue, Kandy", detail.cr2Prefill().get("informantAddress"));
        assertEquals("Registrar Officer", detail.cr2Prefill().get("officerName"));
        assertNotEquals(detail.cr2Prefill().get("permAddressFullText"), detail.cr2Prefill().get("placeInEnglish"));
        assertNotEquals(detail.cr2Prefill().get("placeInEnglish"), detail.cr2Prefill().get("informantAddress"));
    }

    private CreateCaseRequest requestFor(CanonicalFamilyReport report) {
        return new CreateCaseRequest(report, null, null, null, null, null, null, null, null, null);
    }

    private CanonicalFamilyReport canonicalReport(String doctorId, LocalDate dateOfBirth, Integer ageYears) {
        return new CanonicalFamilyReport(
                CanonicalFamilyReport.WorkflowScenario.NATURAL_DEATH_HOME,
                "KANDY-01",
                doctorId,
                true,
                new CanonicalFamilyReport.DeceasedInfo(
                        CanonicalFamilyReport.IdentificationStatus.IDENTIFIED_SRI_LANKAN,
                        "550123456V",
                        null,
                        null,
                        "සුනිල් පෙරේරා",
                        "Sunil Perera",
                        dateOfBirth,
                        ageYears,
                        dateOfBirth == null ? 2 : null,
                        dateOfBirth == null ? 6 : null,
                        "Sri Lankan",
                        Gender.MALE,
                        "Sinhalese",
                        new CanonicalFamilyReport.AddressInfo(
                                "14 Permanent Home, Kandy",
                                "Kandy",
                                "Gangawata Korale",
                                "Kandy Central"),
                        "Retired Teacher",
                        true,
                        "401234567V",
                        "Wijesinghe Perera",
                        "451234567V",
                        "Somawathi Perera"),
                new CanonicalFamilyReport.DeathInfo(
                        LocalDate.of(2026, 4, 20),
                        "10:15",
                        false,
                        "තලාවල පාර, මහනුවර",
                        "22 Lake Road, Kandy",
                        "Kandy",
                        "Gangawata Korale",
                        "Kandy Registration Division",
                        true,
                        "Family-reported stroke",
                        "Kandy Crematorium"),
                null,
                new CanonicalFamilyReport.InformantInfo(
                        CanonicalFamilyReport.InformantCapacity.SON_DAUGHTER,
                        null,
                        "900123456V",
                        "Anula Perera",
                        "88 Informant Avenue, Kandy",
                        "0771234567",
                        "0812345678",
                        "anula@example.com"));
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
}
