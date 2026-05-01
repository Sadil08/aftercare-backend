package com.aftercare.aftercare_portal.service;

import com.aftercare.aftercare_portal.dto.AssignDoctorRequest;
import com.aftercare.aftercare_portal.dto.CanonicalFamilyReport;
import com.aftercare.aftercare_portal.dto.CaseListResponse;
import com.aftercare.aftercare_portal.dto.CaseResponse;
import com.aftercare.aftercare_portal.dto.CreateCaseRequest;
import com.aftercare.aftercare_portal.dto.GnActionRequest;
import com.aftercare.aftercare_portal.dto.IssueB12Request;
import com.aftercare.aftercare_portal.dto.IssueB24Request;
import com.aftercare.aftercare_portal.entity.DeathCase;
import com.aftercare.aftercare_portal.entity.Deceased;
import com.aftercare.aftercare_portal.entity.Sector;
import com.aftercare.aftercare_portal.entity.User;
import com.aftercare.aftercare_portal.entity.document.FormB12;
import com.aftercare.aftercare_portal.entity.document.FormB24;
import com.aftercare.aftercare_portal.entity.document.FormCR2;
import com.aftercare.aftercare_portal.entity.document.FormCR2FamilyInfo;
import com.aftercare.aftercare_portal.enums.DeathCaseStatus;
import com.aftercare.aftercare_portal.enums.Role;
import com.aftercare.aftercare_portal.repository.DeathCaseRepository;
import com.aftercare.aftercare_portal.repository.DeceasedRepository;
import com.aftercare.aftercare_portal.repository.SectorRepository;
import com.aftercare.aftercare_portal.repository.UserRepository;
import com.aftercare.aftercare_portal.repository.CemeteryBookingRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

@Service
public class DeathCaseService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final DeathCaseRepository deathCaseRepository;
    private final DeceasedRepository deceasedRepository;
    private final SectorRepository sectorRepository;
    private final UserRepository userRepository;
    private final CemeteryBookingRepository cemeteryBookingRepository;
    private final HashService hashService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public DeathCaseService(DeathCaseRepository deathCaseRepository, DeceasedRepository deceasedRepository,
            SectorRepository sectorRepository, UserRepository userRepository,
            CemeteryBookingRepository cemeteryBookingRepository,
            HashService hashService, ObjectMapper objectMapper, Validator validator) {
        this.deathCaseRepository = deathCaseRepository;
        this.deceasedRepository = deceasedRepository;
        this.sectorRepository = sectorRepository;
        this.userRepository = userRepository;
        this.cemeteryBookingRepository = cemeteryBookingRepository;
        this.hashService = hashService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @Transactional
    public CaseResponse createCase(User applicant, CreateCaseRequest request) {
        if (!applicant.getRoles().contains(Role.FAMILY)) {
            throw new SecurityException("Only family members can initiate a case.");
        }

        CanonicalFamilyReport familyReport = resolveCanonicalFamilyReport(request, applicant);
        validateCanonicalFamilyReport(familyReport);

        if (familyReport.deceased().nic() != null && deceasedRepository.existsByNic(familyReport.deceased().nic())) {
            throw new IllegalArgumentException("A death case for this NIC already exists.");
        }

        Sector sector = sectorRepository.findByCode(familyReport.sectorCode())
                .orElseThrow(() -> new EntityNotFoundException("Sector not found: " + familyReport.sectorCode()));

        Deceased deceased = mapCanonicalFamilyToDeceased(familyReport, sector);
        DeathCase deathCase = new DeathCase(applicant, deceased, sector);

        String familyReportJson = toJson(familyReport);
        String legacyCr2Snapshot = hasText(request.cr2FormData())
                ? request.cr2FormData()
                : toJson(mapCanonicalFamilyToCr2(familyReport, null, null));
        String cr2Hash = hashService.computeHash(applicant.getNicNo() + familyReportJson);
        deathCase.attachCr2FamilyData(new FormCR2FamilyInfo(applicant, cr2Hash, familyReportJson, legacyCr2Snapshot));

        String doctorId = normalizeDoctorId(
                familyReport.doctorId() != null ? familyReport.doctorId() : request.doctorId());
        if (doctorId != null) {
            User doctor = userRepository.findByDoctorId(doctorId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "No doctor found with ID: " + doctorId + ". Please verify the Doctor ID."));
            deathCase.setAssignedDoctor(doctor);
        }

        deathCase = deathCaseRepository.save(deathCase);
        return mapToResponse(deathCase, applicant);
    }

    @Transactional
    public CaseResponse gnAction(Long caseId, User actingGN, GnActionRequest request) {
        DeathCase deathCase = getCaseById(caseId);

        String action = request.action().trim().toUpperCase();
        switch (action) {
            case "APPROVE" -> deathCase.gnApprove(actingGN);
            case "REQUEST_MEDICAL" -> deathCase.gnRequestMedical(actingGN);
            default -> throw new IllegalArgumentException(
                    "Unknown GN action: " + action + ". Valid values are APPROVE or REQUEST_MEDICAL.");
        }

        deathCase = deathCaseRepository.save(deathCase);
        return mapToResponse(deathCase, actingGN);
    }

    @Transactional
    public CaseResponse assignDoctor(Long caseId, User actingFamily, AssignDoctorRequest request) {
        DeathCase deathCase = getCaseById(caseId);

        User doctor = userRepository.findByDoctorId(request.doctorId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No doctor found with ID: " + request.doctorId() + ". Please verify the Doctor ID."));

        deathCase.familyAssignDoctor(actingFamily, doctor);
        deathCase = deathCaseRepository.save(deathCase);
        return mapToResponse(deathCase, actingFamily);
    }

    @Transactional
    public CaseResponse issueB12(Long caseId, User actingDoctor, IssueB12Request request) {
        DeathCase deathCase = getCaseById(caseId);

        String antecedentJson = toJson(orEmptyList(request.antecedentCauses()));
        String contributoryJson = toJson(orEmptyList(request.contributoryCauses()));
        String payload = caseId + actingDoctor.getNicNo() + request.naturalDeath() + request.icd10Code()
                + request.immediateCause() + antecedentJson + contributoryJson + request.doctorViewedBodyAt()
                + request.doctorDesignation() + request.slmcRegistrationNo();
        String hash = hashService.computeHash(payload);

        deathCase.issueB12(
                actingDoctor,
                hash,
                request.naturalDeath(),
                request.icd10Code(),
                request.immediateCause(),
                antecedentJson,
                contributoryJson,
                request.doctorViewedBodyAt(),
                request.doctorDesignation(),
                request.slmcRegistrationNo());
        deathCase = deathCaseRepository.save(deathCase);

        return mapToResponse(deathCase, actingDoctor);
    }

    @Transactional
    public CaseResponse issueB24(Long caseId, User actingGN, IssueB24Request request) {
        DeathCase deathCase = getCaseById(caseId);
        verifyDocumentIntegrity(deathCase);

        String payload = caseId + actingGN.getNicNo() + toJson(Map.of(
                "b24GramaDivision", request.b24GramaDivision(),
                "b24RegistrarDivision", request.b24RegistrarDivision(),
                "deathDate", request.deathDate().toString(),
                "b24PlaceOfDeath", request.b24PlaceOfDeath(),
                "b24FullName", request.b24FullName(),
                "b24CauseOfDeath", request.b24CauseOfDeath(),
                "b24InformantName", request.b24InformantName(),
                "b24InformantAddress", request.b24InformantAddress(),
                "b24SignDate", request.b24SignDate().toString(),
                "b24GNSignature", request.b24GNSignature()));
        String hash = hashService.computeHash(payload);

        deathCase.issueB24(
                actingGN,
                hash,
                request.b24GramaDivision(),
                request.b24RegistrarDivision(),
                request.b24SerialNo(),
                request.deathDate(),
                request.b24PlaceOfDeath(),
                request.b24FullName(),
                request.b24Sex(),
                request.b24Race(),
                request.b24Age(),
                request.b24Profession(),
                request.b24CauseOfDeath(),
                request.b24InformantName(),
                request.b24InformantAddress(),
                request.b24RegistrarName(),
                request.b24SignedAt(),
                request.b24SignDate(),
                request.b24GNSignature(),
                request.b24Confirmed());

        deathCase = deathCaseRepository.save(deathCase);
        return mapToResponse(deathCase, actingGN);
    }

    @Transactional
    public CaseResponse issueCr2(Long caseId, User actingRegistrar) {
        DeathCase deathCase = getCaseById(caseId);
        verifyDocumentIntegrity(deathCase);

        String serialNumber = generateSerialNumber(deathCase.getSector(), deathCase.getCreatedAt().getYear());
        String payload = caseId + actingRegistrar.getNicNo() + serialNumber;
        String hash = hashService.computeHash(payload);

        deathCase.issueCr2(actingRegistrar, hash, serialNumber);
        deathCase = deathCaseRepository.save(deathCase);

        return mapToResponse(deathCase, actingRegistrar);
    }

    @Transactional(readOnly = true)
    public CaseResponse getCaseDetail(Long caseId, User user) {
        DeathCase deathCase = getCaseById(caseId);
        assertViewAccess(deathCase, user);
        return mapToResponse(deathCase, user);
    }

    @Transactional(readOnly = true)
    public Page<CaseListResponse> getCasesForUser(User user, DeathCaseStatus status, Pageable pageable) {
        if (user.getRoles().contains(Role.FAMILY)) {
            return deathCaseRepository.findByApplicantFamilyMember(user, pageable)
                    .map(this::mapToListResponse);
        }
        if (user.getRoles().contains(Role.GRAMA_NILADHARI)) {
            DeathCaseStatus targetStatus = status != null ? status : DeathCaseStatus.PENDING_GN_REVIEW;
            return deathCaseRepository.findByStatusAndSector(targetStatus, user.getSector(), pageable)
                    .map(this::mapToListResponse);
        }
        if (user.getRoles().contains(Role.DOCTOR)) {
            DeathCaseStatus targetStatus = status != null ? status : DeathCaseStatus.PENDING_B12_MEDICAL;
            return deathCaseRepository.findByStatusAndAssignedDoctor(targetStatus, user, pageable)
                    .map(this::mapToListResponse);
        }
        DeathCaseStatus targetStatus = status != null ? status : DeathCaseStatus.PENDING_REGISTRAR_REVIEW;
        return deathCaseRepository.findByStatus(targetStatus, pageable).map(this::mapToListResponse);
    }

    @Transactional(readOnly = true)
    public CaseResponse getActiveCaseByFamilyNic(String familyNic) {
        DeathCase dc = deathCaseRepository.findFirstByApplicantFamilyMember_NicNoOrderByCreatedAtDesc(familyNic)
                .orElseThrow(
                        () -> new EntityNotFoundException("No active death case found for family NIC: " + familyNic));
        return mapToResponse(dc, dc.getApplicantFamilyMember());
    }

    @Transactional
    public void deleteCase(Long caseId, User user) {
        DeathCase dc = getCaseById(caseId);

        // 1. Authorization: Only the original applicant can delete the case
        if (!dc.getApplicantFamilyMember().getId().equals(user.getId())) {
            throw new SecurityException("Unauthorized: You can only delete your own cases.");
        }

        // 2. State Protection: Prevent deletion of finalized or rejected cases
        if (dc.getStatus() == DeathCaseStatus.CR2_ISSUED_CLOSED) {
            throw new IllegalStateException("Cannot delete a finalized case where a CR-2 has already been issued.");
        }
        if (dc.getStatus() == DeathCaseStatus.REJECTED_UNNATURAL_DEATH) {
            throw new IllegalStateException("Cannot delete a case that has been officially rejected by a doctor.");
        }

        // 3. Cleanup: Manual cleanup for non-cascaded relations
        cemeteryBookingRepository.deleteByDeathCaseId(caseId);

        // 4. Delete the case (Cascades handle deceased, forms, etc.)
        deathCaseRepository.delete(dc);
    }

    private DeathCase getCaseById(Long caseId) {
        return deathCaseRepository.findById(caseId)
                .orElseThrow(() -> new EntityNotFoundException("DeathCase not found: " + caseId));
    }

    private void verifyDocumentIntegrity(DeathCase dc) {
        if (dc.getFormB12() != null && dc.getFormB12().getCryptographicHash() == null) {
            throw new SecurityException("B-12 document appears tampered");
        }
        if (dc.getFormCr2FamilyInfo() != null && dc.getFormCr2FamilyInfo().getCryptographicHash() == null) {
            throw new SecurityException("CR-2 family information appears tampered");
        }
        if (dc.getFormB24() != null && dc.getFormB24().getCryptographicHash() == null) {
            throw new SecurityException("B-24 document appears tampered");
        }
    }

    private void assertViewAccess(DeathCase deathCase, User user) {
        if (user.getRoles().contains(Role.FAMILY)
                && !deathCase.getApplicantFamilyMember().getId().equals(user.getId())) {
            throw new SecurityException("You can only view your own cases.");
        }
        if (user.getRoles().contains(Role.GRAMA_NILADHARI)
                && (user.getSector() == null || !deathCase.getSector().getId().equals(user.getSector().getId()))) {
            throw new SecurityException("You can only view cases in your sector.");
        }
        if (user.getRoles().contains(Role.DOCTOR)
                && (deathCase.getAssignedDoctor() == null
                        || !Objects.equals(deathCase.getAssignedDoctor().getId(), user.getId()))) {
            throw new SecurityException("You can only view cases assigned to you.");
        }
    }

    private CanonicalFamilyReport resolveCanonicalFamilyReport(CreateCaseRequest request, User applicant) {
        if (request.familyReport() != null) {
            return request.familyReport();
        }
        if (!hasText(request.cr2FormData())) {
            throw new IllegalArgumentException("familyReport is required unless legacy cr2FormData is provided.");
        }
        return buildLegacyFamilyReport(
                request.deceasedFullName(),
                request.deceasedNic(),
                request.dateOfBirth(),
                request.dateOfDeath(),
                request.gender(),
                request.address(),
                request.sectorCode(),
                parseJsonMap(request.cr2FormData()),
                applicant);
    }

    private CanonicalFamilyReport resolveCanonicalFamilyReport(DeathCase deathCase) {
        FormCR2FamilyInfo info = deathCase.getFormCr2FamilyInfo();
        if (info != null && hasText(info.getFamilyReportJson())) {
            try {
                return objectMapper.readValue(info.getFamilyReportJson(), CanonicalFamilyReport.class);
            } catch (JsonProcessingException ignored) {
                // Fall through to legacy reconstruction.
            }
        }

        Map<String, Object> legacyMap = info != null ? parseJsonMap(info.getCr2FormData()) : Map.of();
        Deceased deceased = deathCase.getDeceased();
        return buildLegacyFamilyReport(
                deceased.getDisplayFullName(),
                deceased.getNic(),
                deceased.getDateOfBirth(),
                deceased.getDateOfDeath(),
                deceased.getGender(),
                deceased.getDisplayAddress(),
                deathCase.getSector().getCode(),
                legacyMap,
                deathCase.getApplicantFamilyMember());
    }

    private void validateCanonicalFamilyReport(CanonicalFamilyReport familyReport) {
        Set<ConstraintViolation<CanonicalFamilyReport>> violations = validator.validate(familyReport);
        if (!violations.isEmpty()) {
            StringJoiner joiner = new StringJoiner("; ");
            for (ConstraintViolation<CanonicalFamilyReport> violation : violations) {
                joiner.add(violation.getMessage());
            }
            throw new IllegalArgumentException(joiner.toString());
        }
        if (familyReport.deceased().dateOfBirth() != null
                && familyReport.death().date().isBefore(familyReport.deceased().dateOfBirth())) {
            throw new IllegalArgumentException("Date of death cannot be before date of birth.");
        }
    }

    private Deceased mapCanonicalFamilyToDeceased(CanonicalFamilyReport familyReport, Sector sector) {
        CanonicalFamilyReport.DeceasedInfo deceased = familyReport.deceased();
        CanonicalFamilyReport.MaternalInfo maternal = familyReport.maternal();
        return new Deceased(
                deceased.fullNameEnglish(),
                deceased.nic(),
                deceased.identificationStatus(),
                deceased.passportCountry(),
                deceased.passportNumber(),
                deceased.fullNameOfficialLanguage(),
                deceased.dateOfBirth(),
                deceased.ageYears(),
                deceased.ageMonths(),
                deceased.ageDays(),
                deceased.nationality(),
                familyReport.death().date(),
                deceased.gender(),
                deceased.permanentAddress().fullText(),
                deceased.permanentAddress().district(),
                deceased.permanentAddress().dsDivision(),
                deceased.permanentAddress().gnDivision(),
                deceased.race(),
                deceased.profession(),
                deceased.pensionStatus(),
                deceased.fatherNic(),
                deceased.fatherName(),
                deceased.motherNic(),
                deceased.motherName(),
                maternal != null ? maternal.wasPregnantAtDeath() : null,
                maternal != null ? maternal.gaveBirthWithin42Days() : null,
                maternal != null ? maternal.hadAbortion() : null,
                maternal != null ? maternal.daysSinceBirthOrAbortion() : null,
                sector);
    }

    private CanonicalFamilyReport buildLegacyFamilyReport(String deceasedFullName, String deceasedNic,
            LocalDate dateOfBirth, LocalDate dateOfDeath, com.aftercare.aftercare_portal.enums.Gender gender,
            String address, String sectorCode, Map<String, Object> legacyMap, User applicant) {
        String officialPlace = stringValue(legacyMap.get("placeInSinhalaOrTamil"));
        String placeEnglish = fallback(stringValue(legacyMap.get("placeInEnglish")), address);
        String informantAddress = fallback(stringValue(legacyMap.get("informantAddress")), address);
        String nationality = hasText(stringValue(legacyMap.get("foreignerCountry")))
                ? stringValue(legacyMap.get("foreignerCountry"))
                : "Sri Lankan";
        CanonicalFamilyReport.IdentificationStatus identificationStatus = deceasedNic != null && !deceasedNic.isBlank()
                ? CanonicalFamilyReport.IdentificationStatus.IDENTIFIED_SRI_LANKAN
                : hasText(stringValue(legacyMap.get("foreignerPassport")))
                        ? CanonicalFamilyReport.IdentificationStatus.IDENTIFIED_FOREIGNER
                        : CanonicalFamilyReport.IdentificationStatus.NOT_IDENTIFIED;

        return new CanonicalFamilyReport(
                CanonicalFamilyReport.WorkflowScenario.NATURAL_DEATH_HOME,
                sectorCode,
                normalizeDoctorId(stringValue(legacyMap.get("doctorId"))),
                boolValue(legacyMap.get("declarationConfirmed"), true),
                new CanonicalFamilyReport.DeceasedInfo(
                        identificationStatus,
                        deceasedNic,
                        stringValue(legacyMap.get("foreignerCountry")),
                        stringValue(legacyMap.get("foreignerPassport")),
                        stringValue(legacyMap.get("nameOfficialLang")),
                        fallback(deceasedFullName, stringValue(legacyMap.get("nameEnglish"))),
                        dateOfBirth,
                        intValue(legacyMap.get("ageYears")),
                        intValue(legacyMap.get("ageMonths")),
                        intValue(legacyMap.get("ageDays")),
                        nationality,
                        gender,
                        stringValue(legacyMap.get("deceasedRace")),
                        new CanonicalFamilyReport.AddressInfo(
                                fallback(stringValue(legacyMap.get("permAddressFullText")), address),
                                fallback(stringValue(legacyMap.get("permAddressDistrict")),
                                        stringValue(legacyMap.get("district"))),
                                fallback(stringValue(legacyMap.get("permAddressDs")),
                                        stringValue(legacyMap.get("dsDivision"))),
                                fallback(stringValue(legacyMap.get("permAddressGn")),
                                        stringValue(legacyMap.get("regDivision")))),
                        stringValue(legacyMap.get("profession")),
                        boolValueOrNull(legacyMap.get("pensionStatus")),
                        stringValue(legacyMap.get("fatherNic")),
                        stringValue(legacyMap.get("fatherName")),
                        stringValue(legacyMap.get("motherNic")),
                        stringValue(legacyMap.get("motherName"))),
                new CanonicalFamilyReport.DeathInfo(
                        dateOfDeath,
                        stringValue(legacyMap.get("timeOfDeath")),
                        false,
                        officialPlace,
                        fallback(placeEnglish, address),
                        fallback(stringValue(legacyMap.get("district")), ""),
                        fallback(stringValue(legacyMap.get("dsDivision")), ""),
                        fallback(stringValue(legacyMap.get("regDivision")), ""),
                        boolValueOrNull(legacyMap.get("causeEstablished")),
                        fallback(stringValue(legacyMap.get("causeOfDeathDetail")),
                                stringValue(legacyMap.get("causeOfDeath"))),
                        stringValue(legacyMap.get("burialPlace"))),
                new CanonicalFamilyReport.MaternalInfo(
                        boolValueOrNull(legacyMap.get("wasPregnant")),
                        boolValueOrNull(legacyMap.get("recentBirth")),
                        boolValueOrNull(legacyMap.get("recentAbortion")),
                        intValue(legacyMap.get("maternalTimelineDays"))),
                new CanonicalFamilyReport.InformantInfo(
                        mapInformantCapacity(stringValue(legacyMap.get("informantCapacity"))),
                        stringValue(legacyMap.get("informantOtherCapacityText")),
                        fallback(stringValue(legacyMap.get("informantId")), applicant.getNicNo()),
                        fallback(stringValue(legacyMap.get("informantName")), applicant.getFullName()),
                        fallback(informantAddress, address),
                        fallback(stringValue(legacyMap.get("informantPhone")), applicant.getPhone()),
                        stringValue(legacyMap.get("informantLandline")),
                        fallback(stringValue(legacyMap.get("informantEmail")), applicant.getEmail())));
    }

    private CaseResponse mapToResponse(DeathCase dc, User viewer) {
        CanonicalFamilyReport familyReport = resolveCanonicalFamilyReport(dc);
        return new CaseResponse(
                dc.getId(),
                dc.getStatus().name(),
                dc.getApplicantFamilyMember().getFullName(),
                dc.getApplicantFamilyMember().getNicNo(),
                dc.getDeceased().getDisplayFullName(),
                dc.getDeceased().getFullNameOfficialLanguage(),
                dc.getDeceased().getNic(),
                dc.getDeceased().getDateOfBirth(),
                dc.getDeceased().getDateOfDeath(),
                dc.getDeceased().getGender().name(),
                dc.getDeceased().getDisplayAddress(),
                dc.getSector().getCode(),
                dc.getSector().getName(),
                dc.getAssignedDoctor() != null ? dc.getAssignedDoctor().getDoctorId() : null,
                dc.getAssignedDoctor() != null ? dc.getAssignedDoctor().getFullName() : null,
                objectMapper.convertValue(familyReport, MAP_TYPE),
                mapCanonicalFamilyToB12Header(familyReport),
                mapCanonicalFamilyToB24(familyReport, dc.getFormB12(), viewer),
                mapCanonicalFamilyToCr2(familyReport, dc.getFormB12(), viewer),
                mapB12(dc.getFormB12()),
                mapB24(dc.getFormB24()),
                mapCr2Family(dc.getFormCr2FamilyInfo(), familyReport),
                mapCr2(dc.getFormCr2()),
                dc.getCreatedAt(),
                dc.getUpdatedAt());
    }

    private CaseListResponse mapToListResponse(DeathCase dc) {
        String cause = dc.getFormB12() != null ? dc.getFormB12().getImmediateCause() : null;
        String b12DoctorName = dc.getFormB12() != null ? dc.getFormB12().getIssuedBy().getFullName() : null;
        String b12DoctorId = dc.getFormB12() != null ? dc.getFormB12().getIssuedBy().getDoctorId() : null;
        String b12Icd10Code = dc.getFormB12() != null ? dc.getFormB12().getIcd10Code() : null;
        return new CaseListResponse(
                dc.getId(),
                dc.getStatus().name(),
                dc.getDeceased().getDisplayFullName(),
                dc.getDeceased().getNic(),
                dc.getApplicantFamilyMember() != null ? dc.getApplicantFamilyMember().getFullName() : null,
                cause,
                b12DoctorName,
                b12DoctorId,
                b12Icd10Code,
                dc.getSector().getName(),
                dc.getCreatedAt(),
                dc.getUpdatedAt());
    }

    private Map<String, Object> mapCanonicalFamilyToB12Header(CanonicalFamilyReport familyReport) {
        Map<String, Object> mapped = new LinkedHashMap<>();
        CanonicalFamilyReport.DeceasedInfo deceased = familyReport.deceased();
        CanonicalFamilyReport.DeathInfo death = familyReport.death();
        AgeParts age = resolveAgeParts(deceased, death.date());

        mapped.put("identificationStatus", deceased.identificationStatus().name());
        mapped.put("deceasedNic", deceased.nic());
        mapped.put("passportCountry", deceased.passportCountry());
        mapped.put("passportNumber", deceased.passportNumber());
        mapped.put("fullNameOfficialLanguage", deceased.fullNameOfficialLanguage());
        mapped.put("fullNameEnglish", deceased.fullNameEnglish());
        mapped.put("dateOfBirth", deceased.dateOfBirth());
        mapped.put("dateOfDeath", death.date());
        mapped.put("timeOfDeath", death.time());
        mapped.put("gender", deceased.gender().name());
        mapped.put("age", age.toText());
        mapped.put("nationality", deceased.nationality());
        mapped.put("permanentAddressFullText", deceased.permanentAddress().fullText());
        mapped.put("permanentDistrict", deceased.permanentAddress().district());
        mapped.put("permanentDsDivision", deceased.permanentAddress().dsDivision());
        mapped.put("permanentGnDivision", deceased.permanentAddress().gnDivision());
        mapped.put("placeOfDeath", death.placeEnglish());
        mapped.put("registrationDivision", death.registrationDivision());
        mapped.put("race", deceased.race());
        return mapped;
    }

    private Map<String, Object> mapCanonicalFamilyToB24(CanonicalFamilyReport familyReport, FormB12 formB12, User viewer) {
        Map<String, Object> mapped = new LinkedHashMap<>();
        CanonicalFamilyReport.DeceasedInfo deceased = familyReport.deceased();
        CanonicalFamilyReport.DeathInfo death = familyReport.death();
        AgeParts age = resolveAgeParts(deceased, death.date());

        mapped.put("b24GramaDivision", viewer != null && viewer.getSector() != null
                ? viewer.getSector().getName()
                : deceased.permanentAddress().gnDivision());
        mapped.put("b24RegistrarDivision", death.registrationDivision());
        mapped.put("b24SerialNo", null);
        mapped.put("b24DeathYear", death.date().getYear());
        mapped.put("b24DeathMonth", death.date().getMonthValue());
        mapped.put("b24DeathDay", death.date().getDayOfMonth());
        mapped.put("b24PlaceOfDeath", death.placeEnglish());
        mapped.put("b24FullName", deceased.fullNameEnglish());
        mapped.put("b24Sex", deceased.gender().name().toLowerCase());
        mapped.put("b24Race", deceased.race());
        mapped.put("b24Age", age.toText());
        mapped.put("b24Profession", deceased.profession());
        mapped.put("b24CauseOfDeath", authoritativeCause(formB12, familyReport));
        mapped.put("b24InformantName", familyReport.informant().fullName());
        mapped.put("b24InformantAddress", familyReport.informant().postalAddress());
        mapped.put("b24RegistrarName", null);
        mapped.put("b24SignedAt", viewer != null && viewer.getSector() != null
                ? viewer.getSector().getName()
                : death.registrationDivision());
        mapped.put("b24SignDate", LocalDate.now());
        mapped.put("b24GNSignature", viewer != null ? viewer.getFullName() : null);
        mapped.put("b24Confirmed", false);
        return mapped;
    }

    private Map<String, Object> mapCanonicalFamilyToCr2(CanonicalFamilyReport familyReport, FormB12 formB12, User viewer) {
        Map<String, Object> mapped = new LinkedHashMap<>();
        CanonicalFamilyReport.DeceasedInfo deceased = familyReport.deceased();
        CanonicalFamilyReport.DeathInfo death = familyReport.death();
        CanonicalFamilyReport.MaternalInfo maternal = familyReport.maternal();
        CanonicalFamilyReport.InformantInfo informant = familyReport.informant();
        AgeParts age = resolveAgeParts(deceased, death.date());

        mapped.put("typeOfDeath", "normal");
        mapped.put("deathYear", death.date().getYear());
        mapped.put("deathMonth", death.date().getMonthValue());
        mapped.put("deathDay", death.date().getDayOfMonth());
        mapped.put("district", death.placeDistrict());
        mapped.put("dsDivision", death.placeDsDivision());
        mapped.put("regDivision", death.registrationDivision());
        mapped.put("placeInSinhalaOrTamil", death.placeOfficialLanguage());
        mapped.put("placeInEnglish", death.placeEnglish());
        mapped.put("timeOfDeath", death.time());
        mapped.put("deathLocation", "outside");
        mapped.put("causeEstablished", formB12 != null ? "yes" : yesNo(death.causeKnownByFamily()));
        mapped.put("causeOfDeath", authoritativeCause(formB12, familyReport));
        mapped.put("icdCode", formB12 != null ? formB12.getIcd10Code() : null);
        mapped.put("burialPlace", death.burialOrCremationPlace());
        mapped.put("identificationStatus",
                deceased.identificationStatus() == CanonicalFamilyReport.IdentificationStatus.NOT_IDENTIFIED
                        ? "not_identified"
                        : "identified");
        mapped.put("deceasedNic", deceased.nic());
        mapped.put("foreignerCountry", deceased.passportCountry());
        mapped.put("foreignerPassport", deceased.passportNumber());
        mapped.put("dobYear", deceased.dateOfBirth() != null ? deceased.dateOfBirth().getYear() : null);
        mapped.put("dobMonth", deceased.dateOfBirth() != null ? deceased.dateOfBirth().getMonthValue() : null);
        mapped.put("dobDay", deceased.dateOfBirth() != null ? deceased.dateOfBirth().getDayOfMonth() : null);
        mapped.put("ageYears", age.years());
        mapped.put("ageMonths", age.months());
        mapped.put("ageDays", age.days());
        mapped.put("deceasedGender", deceased.gender().name().toLowerCase());
        mapped.put("deceasedRace", deceased.race());
        mapped.put("nameOfficialLang", deceased.fullNameOfficialLanguage());
        mapped.put("nameEnglish", deceased.fullNameEnglish());
        mapped.put("permAddressFullText", deceased.permanentAddress().fullText());
        mapped.put("permAddressDistrict", deceased.permanentAddress().district());
        mapped.put("permAddressDs", deceased.permanentAddress().dsDivision());
        mapped.put("permAddressGn", deceased.permanentAddress().gnDivision());
        mapped.put("profession", deceased.profession());
        mapped.put("pensionStatus", yesNo(deceased.pensionStatus()));
        mapped.put("fatherNic", deceased.fatherNic());
        mapped.put("fatherName", deceased.fatherName());
        mapped.put("motherNic", deceased.motherNic());
        mapped.put("motherName", deceased.motherName());
        mapped.put("wasPregnant", maternal != null ? yesNo(maternal.wasPregnantAtDeath()) : null);
        mapped.put("recentBirth", maternal != null ? yesNo(maternal.gaveBirthWithin42Days()) : null);
        mapped.put("recentAbortion", maternal != null ? yesNo(maternal.hadAbortion()) : null);
        mapped.put("maternalTimelineDays", maternal != null ? maternal.daysSinceBirthOrAbortion() : null);
        mapped.put("causeOfDeathDetail", authoritativeCause(formB12, familyReport));
        mapped.put("isNaturalDeath", formB12 != null ? yesNo(formB12.isNaturalDeath()) : "yes");
        mapped.put("suddenDeathReasons", null);
        mapped.put("opinionAboutDeath", null);
        mapped.put("otherInformation", death.familyNarrative());
        mapped.put("informantCapacity", mapInformantCapacityToUi(informant.capacity()));
        mapped.put("informantOtherCapacityText", informant.otherCapacityText());
        mapped.put("informantId", informant.nicOrPassport());
        mapped.put("informantName", informant.fullName());
        mapped.put("informantAddress", informant.postalAddress());
        mapped.put("informantPhone", informant.mobile());
        mapped.put("informantLandline", informant.landline());
        mapped.put("informantEmail", informant.email());
        mapped.put("declarationConfirmed", true);
        mapped.put("declarationDate", LocalDate.now());
        mapped.put("informantSignatureName", informant.fullName());
        mapped.put("informantSignatureAddress", informant.postalAddress());
        mapped.put("officerId", viewer != null ? viewer.getNicNo() : null);
        mapped.put("officerName", viewer != null ? viewer.getFullName() : null);
        mapped.put("officerAddress", null);
        mapped.put("officerDivision",
                viewer != null && viewer.getSector() != null ? viewer.getSector().getName() : death.registrationDivision());
        mapped.put("officerDate", viewer != null ? LocalDate.now() : null);
        return mapped;
    }

    private Map<String, Object> mapB12(FormB12 b12) {
        if (b12 == null) {
            return null;
        }
        Map<String, Object> mapped = new LinkedHashMap<>();
        mapped.put("documentType", b12.getDocumentType());
        mapped.put("issuedAt", b12.getIssuedAt());
        mapped.put("issuedBy", b12.getIssuedBy().getFullName());
        mapped.put("naturalDeath", b12.isNaturalDeath());
        mapped.put("icd10Code", b12.getIcd10Code());
        mapped.put("immediateCause", b12.getImmediateCause());
        mapped.put("primaryCause", b12.getPrimaryCause());
        mapped.put("antecedentCauses", parseJsonList(b12.getAntecedentCausesJson()));
        mapped.put("contributoryCauses", parseJsonList(b12.getContributoryCausesJson()));
        mapped.put("doctorViewedBodyAt", b12.getDoctorViewedBodyAt());
        mapped.put("doctorDesignation", b12.getDoctorDesignation());
        mapped.put("slmcRegistrationNo", b12.getSlmcRegistrationNo());
        return mapped;
    }

    private Map<String, Object> mapB24(FormB24 b24) {
        if (b24 == null) {
            return null;
        }
        Map<String, Object> mapped = new LinkedHashMap<>();
        mapped.put("documentType", b24.getDocumentType());
        mapped.put("issuedAt", b24.getIssuedAt());
        mapped.put("issuedBy", b24.getIssuedBy().getFullName());
        mapped.put("b24GramaDivision", b24.getGramaDivision());
        mapped.put("b24RegistrarDivision", b24.getRegistrarDivision());
        mapped.put("b24SerialNo", b24.getSerialNo());
        mapped.put("deathDate", b24.getDeathDate());
        mapped.put("b24PlaceOfDeath", b24.getPlaceOfDeath());
        mapped.put("b24FullName", b24.getFullName());
        mapped.put("b24Sex", b24.getSex());
        mapped.put("b24Race", b24.getRace());
        mapped.put("b24Age", b24.getAge());
        mapped.put("b24Profession", b24.getProfession());
        mapped.put("b24CauseOfDeath", b24.getCauseOfDeath());
        mapped.put("b24InformantName", b24.getInformantName());
        mapped.put("b24InformantAddress", b24.getInformantAddress());
        mapped.put("b24RegistrarName", b24.getRegistrarName());
        mapped.put("b24SignedAt", b24.getSignedAt());
        mapped.put("b24SignDate", b24.getSignDate());
        mapped.put("b24GNSignature", b24.getGnSignature());
        mapped.put("b24Confirmed", b24.isConfirmed());
        return mapped;
    }

    private Map<String, Object> mapCr2Family(FormCR2FamilyInfo info, CanonicalFamilyReport familyReport) {
        if (info == null) {
            return null;
        }
        Map<String, Object> mapped = new LinkedHashMap<>();
        mapped.put("documentType", info.getDocumentType());
        mapped.put("issuedAt", info.getIssuedAt());
        mapped.put("issuedBy", info.getIssuedBy().getFullName());
        mapped.put("familyReportJson", info.getFamilyReportJson());
        mapped.put("cr2FormData", info.getCr2FormData());
        mapped.put("familyReport", objectMapper.convertValue(familyReport, MAP_TYPE));
        mapped.put("legacyParsedData", parseJsonMap(info.getCr2FormData()));
        return mapped;
    }

    private Map<String, Object> mapCr2(FormCR2 cr2) {
        if (cr2 == null) {
            return null;
        }
        Map<String, Object> mapped = new LinkedHashMap<>();
        mapped.put("documentType", cr2.getDocumentType());
        mapped.put("issuedAt", cr2.getIssuedAt());
        mapped.put("issuedBy", cr2.getIssuedBy().getFullName());
        mapped.put("certificateSerialNumber", cr2.getCertificateSerialNumber());
        return mapped;
    }

    private String generateSerialNumber(Sector sector, int year) {
        long count = deathCaseRepository.count() + 1;
        return String.format("B2-%d-%s-%06d", year, sector.getDistrict().substring(0, 3).toUpperCase(), count);
    }

    private String authoritativeCause(FormB12 formB12, CanonicalFamilyReport familyReport) {
        if (formB12 != null) {
            return formB12.getImmediateCause();
        }
        return familyReport.death().familyNarrative();
    }

    private AgeParts resolveAgeParts(CanonicalFamilyReport.DeceasedInfo deceased, LocalDate deathDate) {
        if (deceased.dateOfBirth() != null) {
            Period period = Period.between(deceased.dateOfBirth(), deathDate);
            return new AgeParts(Math.max(period.getYears(), 0), Math.max(period.getMonths(), 0), Math.max(period.getDays(), 0));
        }
        return new AgeParts(
                defaultInt(deceased.ageYears()),
                defaultInt(deceased.ageMonths()),
                defaultInt(deceased.ageDays()));
    }

    private CanonicalFamilyReport.InformantCapacity mapInformantCapacity(String raw) {
        if (!hasText(raw)) {
            return CanonicalFamilyReport.InformantCapacity.RELATIVE;
        }
        String normalized = raw.trim().toUpperCase().replace('-', '_').replace(' ', '_');
        return switch (normalized) {
            case "HUSBAND_WIFE", "HUSBAND/WIFE" -> CanonicalFamilyReport.InformantCapacity.HUSBAND_WIFE;
            case "FATHER_MOTHER", "FATHER/MOTHER" -> CanonicalFamilyReport.InformantCapacity.FATHER_MOTHER;
            case "SON_DAUGHTER", "SON/DAUGHTER" -> CanonicalFamilyReport.InformantCapacity.SON_DAUGHTER;
            case "BROTHER_SISTER", "BROTHER/SISTER" -> CanonicalFamilyReport.InformantCapacity.BROTHER_SISTER;
            case "OTHER" -> CanonicalFamilyReport.InformantCapacity.OTHER;
            default -> CanonicalFamilyReport.InformantCapacity.RELATIVE;
        };
    }

    private String mapInformantCapacityToUi(CanonicalFamilyReport.InformantCapacity capacity) {
        return switch (capacity) {
            case HUSBAND_WIFE -> "husband_wife";
            case FATHER_MOTHER -> "father_mother";
            case SON_DAUGHTER -> "son_daughter";
            case BROTHER_SISTER -> "brother_sister";
            case OTHER -> "other";
            default -> "relative";
        };
    }

    private Map<String, Object> parseJsonMap(String raw) {
        if (!hasText(raw)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(raw, MAP_TYPE);
        } catch (JsonProcessingException ignored) {
            return Map.of();
        }
    }

    private List<String> parseJsonList(String raw) {
        if (!hasText(raw)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(raw, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException ignored) {
            return List.of();
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize payload.", e);
        }
    }

    private List<String> orEmptyList(List<String> items) {
        return items == null ? List.of() : new ArrayList<>(items);
    }

    private String normalizeDoctorId(String doctorId) {
        return hasText(doctorId) ? doctorId.trim() : null;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Integer intValue(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        return Integer.valueOf(String.valueOf(value));
    }

    private Boolean boolValueOrNull(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        String normalized = String.valueOf(value).trim().toLowerCase();
        if ("yes".equals(normalized) || "true".equals(normalized)) {
            return true;
        }
        if ("no".equals(normalized) || "false".equals(normalized)) {
            return false;
        }
        return null;
    }

    private boolean boolValue(Object value, boolean defaultValue) {
        Boolean parsed = boolValueOrNull(value);
        return parsed != null ? parsed : defaultValue;
    }

    private String yesNo(Boolean value) {
        if (value == null) {
            return null;
        }
        return value ? "yes" : "no";
    }

    private String fallback(String first, String second) {
        return hasText(first) ? first : second;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private int defaultInt(Integer value) {
        return value != null ? value : 0;
    }

    private record AgeParts(int years, int months, int days) {
        private String toText() {
            return years + "y " + months + "m " + days + "d";
        }
    }
}
