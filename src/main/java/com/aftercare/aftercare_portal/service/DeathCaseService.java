package com.aftercare.aftercare_portal.service;

import com.aftercare.aftercare_portal.dto.*;
import com.aftercare.aftercare_portal.entity.*;
import com.aftercare.aftercare_portal.entity.document.FormCR2FamilyInfo;
import com.aftercare.aftercare_portal.entity.document.FormB12;
import com.aftercare.aftercare_portal.entity.document.FormCR2;
import com.aftercare.aftercare_portal.entity.document.FormB24;
import com.aftercare.aftercare_portal.enums.DeathCaseStatus;
import com.aftercare.aftercare_portal.enums.Role;
import com.aftercare.aftercare_portal.repository.DeathCaseRepository;
import com.aftercare.aftercare_portal.repository.DeceasedRepository;
import com.aftercare.aftercare_portal.repository.SectorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class DeathCaseService {

    private final DeathCaseRepository deathCaseRepository;
    private final DeceasedRepository deceasedRepository;
    private final SectorRepository sectorRepository;
    private final HashService hashService;

    public DeathCaseService(DeathCaseRepository deathCaseRepository, DeceasedRepository deceasedRepository,
            SectorRepository sectorRepository, HashService hashService) {
        this.deathCaseRepository = deathCaseRepository;
        this.deceasedRepository = deceasedRepository;
        this.sectorRepository = sectorRepository;
        this.hashService = hashService;
    }

    // ──── Phase 1: Family Initiates Case ────
    @Transactional
    public CaseResponse createCase(User applicant, CreateCaseRequest request) {
        if (!applicant.getRoles().contains(Role.FAMILY)) {
            throw new SecurityException("Only family members can initiate a case.");
        }

        if (request.deceasedNic() != null && deceasedRepository.existsByNic(request.deceasedNic())) {
            throw new IllegalArgumentException("A death case for this NIC already exists.");
        }

        Sector sector = sectorRepository.findByCode(request.sectorCode())
                .orElseThrow(() -> new EntityNotFoundException("Sector not found: " + request.sectorCode()));

        Deceased deceased = new Deceased(
                request.deceasedFullName(),
                request.deceasedNic(),
                request.dateOfBirth(),
                request.dateOfDeath(),
                request.gender(),
                request.address(),
                sector);

        DeathCase deathCase = new DeathCase(applicant, deceased, sector);
        deathCase = deathCaseRepository.save(deathCase);

        return mapToResponse(deathCase);
    }

    // ──── Phase 2: Doctor Issues B-12 (Medical Certification) ────
    @Transactional
    public CaseResponse issueB12(Long caseId, User actingDoctor, IssueB12Request request) {
        DeathCase deathCase = getCaseById(caseId);

        String payload = caseId + actingDoctor.getNicNo() + request.naturalDeath() + request.icd10Code()
                + request.primaryCause();
        String hash = hashService.computeHash(payload);

        deathCase.issueB12(actingDoctor, hash, request.naturalDeath(), request.icd10Code(), request.primaryCause());
        deathCase = deathCaseRepository.save(deathCase);

        return mapToResponse(deathCase);
    }

    // ──── Phase 3: GN Issues B-24 (Identity & Residence Verification) ────
    @Transactional
    public CaseResponse issueB24(Long caseId, User actingGN, IssueB24Request request) {
        DeathCase deathCase = getCaseById(caseId);

        if (!actingGN.getSector().getId().equals(deathCase.getSector().getId())) {
            throw new SecurityException("You can only verify cases in your assigned sector.");
        }

        String payload = caseId + actingGN.getNicNo() + request.identityVerified() + request.residenceVerified();
        String hash = hashService.computeHash(payload);

        deathCase.issueB24(actingGN, hash, request.identityVerified(), request.residenceVerified());
        deathCase = deathCaseRepository.save(deathCase);

        return mapToResponse(deathCase);
    }

    // ──── Phase 4: Family Submits CR-2 (Declaration) ────
    @Transactional
    public CaseResponse submitCr2Family(Long caseId, User actingCitizen, SubmitCr2FamilyRequest request) {
        DeathCase deathCase = getCaseById(caseId);

        String payload = caseId + actingCitizen.getNicNo() + request.cr2FormData();
        String hash = hashService.computeHash(payload);

        deathCase.submitCr2Family(actingCitizen, hash, request.cr2FormData());
        deathCase = deathCaseRepository.save(deathCase);

        return mapToResponse(deathCase);
    }

    // ──── Phase 5: Registrar Issues CR-2 (Death Certificate) ────
    @Transactional
    public CaseResponse issueCr2(Long caseId, User actingRegistrar) {
        DeathCase deathCase = getCaseById(caseId);

        // Re-verify document integrity
        verifyDocumentIntegrity(deathCase);

        String serialNumber = generateSerialNumber(deathCase.getSector(), deathCase.getCreatedAt().getYear());

        String payload = caseId + actingRegistrar.getNicNo() + serialNumber;
        String hash = hashService.computeHash(payload);

        deathCase.issueCr2(actingRegistrar, hash, serialNumber);
        deathCase = deathCaseRepository.save(deathCase);

        return mapToResponse(deathCase);
    }

    // ──── Case Tracking ────

    @Transactional(readOnly = true)
    public CaseResponse getCaseDetail(Long caseId, User user) {
        DeathCase deathCase = getCaseById(caseId);

        // RBAC check for viewing
        if (user.getRoles().contains(Role.FAMILY)
                && !deathCase.getApplicantFamilyMember().getId().equals(user.getId())) {
            throw new SecurityException("You can only view your own cases.");
        }
        if (user.getRoles().contains(Role.GRAMA_NILADHARI)
                && !deathCase.getSector().getId().equals(user.getSector().getId())) {
            throw new SecurityException("You can only view cases in your sector.");
        }

        return mapToResponse(deathCase);
    }

    @Transactional(readOnly = true)
    public Page<CaseListResponse> getCasesForUser(User user, DeathCaseStatus status, Pageable pageable) {
        if (user.getRoles().contains(Role.FAMILY)) {
            return deathCaseRepository.findByApplicantFamilyMember(user, pageable)
                    .map(this::mapToListResponse);
        } else if (user.getRoles().contains(Role.GRAMA_NILADHARI)) {
            if (status != null) {
                return deathCaseRepository.findByStatusAndSector(status, user.getSector(), pageable)
                        .map(this::mapToListResponse);
            }
            return deathCaseRepository
                    .findByStatusAndSector(DeathCaseStatus.PENDING_B24_GN, user.getSector(), pageable)
                    .map(this::mapToListResponse);
        } else if (user.getRoles().contains(Role.DOCTOR)) {
            DeathCaseStatus targetStatus = (status != null) ? status : DeathCaseStatus.PENDING_B12_MEDICAL;
            return deathCaseRepository.findByStatus(targetStatus, pageable).map(this::mapToListResponse);
        } else {
            // REGISTRAR
            DeathCaseStatus targetStatus = (status != null) ? status : DeathCaseStatus.PENDING_REGISTRAR_REVIEW;
            return deathCaseRepository.findByStatus(targetStatus, pageable).map(this::mapToListResponse);
        }
    }

    @Transactional(readOnly = true)
    public CaseResponse getActiveCaseByFamilyNic(String familyNic) {
        DeathCase dc = deathCaseRepository.findFirstByApplicantFamilyMember_NicNoOrderByCreatedAtDesc(familyNic)
                .orElseThrow(() -> new EntityNotFoundException("No active death case found for family NIC: " + familyNic));
        return mapToResponse(dc);
    }

    // ──── Private Helpers ────

    private DeathCase getCaseById(Long caseId) {
        return deathCaseRepository.findById(caseId)
                .orElseThrow(() -> new EntityNotFoundException("DeathCase not found: " + caseId));
    }

    private void verifyDocumentIntegrity(DeathCase dc) {
        if (dc.getFormB12() == null || dc.getFormB12().getCryptographicHash() == null)
            throw new SecurityException("B-12 missing or tampered");
        if (dc.getFormB24() == null || dc.getFormB24().getCryptographicHash() == null)
            throw new SecurityException("B-24 missing or tampered");
        // CR-2 Family Info is only present for new-flow cases; legacy B-11 cases won't have it
        if (dc.getFormCr2FamilyInfo() != null && dc.getFormCr2FamilyInfo().getCryptographicHash() == null)
            throw new SecurityException("CR-2 Family Info tampered");
    }

    private String generateSerialNumber(Sector sector, int year) {
        long count = deathCaseRepository.count() + 1;
        return String.format("B2-%d-%s-%06d", year, sector.getDistrict().substring(0, 3).toUpperCase(), count);
    }

    private CaseResponse mapToResponse(DeathCase dc) {
        return new CaseResponse(
                dc.getId(),
                dc.getStatus().name(),
                dc.getApplicantFamilyMember().getFullName(),
                dc.getApplicantFamilyMember().getNicNo(),
                dc.getDeceased().getFullName(),
                dc.getDeceased().getNic(),
                dc.getDeceased().getDateOfDeath(),
                dc.getDeceased().getGender().name(),
                dc.getDeceased().getAddress(),
                dc.getSector().getCode(),
                dc.getSector().getName(),
                mapB12(dc.getFormB12()),
                mapB24(dc.getFormB24()),
                mapCr2Family(dc.getFormCr2FamilyInfo()),
                mapCr2(dc.getFormCr2()),
                dc.getCreatedAt(),
                dc.getUpdatedAt());
    }

    private CaseListResponse mapToListResponse(DeathCase dc) {
        String cause = dc.getFormB12() != null ? dc.getFormB12().getPrimaryCause() : null;
        return new CaseListResponse(
                dc.getId(),
                dc.getStatus().name(),
                dc.getDeceased().getFullName(),
                dc.getDeceased().getNic(),
                dc.getApplicantFamilyMember() != null ? dc.getApplicantFamilyMember().getFullName() : null,
                cause,
                dc.getSector().getName(),
                dc.getCreatedAt(),
                dc.getUpdatedAt());
    }

    private Map<String, Object> mapB12(FormB12 b12) {
        if (b12 == null)
            return null;
        return Map.of(
                "documentType", b12.getDocumentType(),
                "issuedAt", b12.getIssuedAt(),
                "issuedBy", b12.getIssuedBy().getFullName(),
                "naturalDeath", b12.isNaturalDeath(),
                "icd10Code", b12.getIcd10Code(),
                "primaryCause", b12.getPrimaryCause());
    }

    private Map<String, Object> mapB24(FormB24 b24) {
        if (b24 == null)
            return null;
        return Map.of(
                "documentType", b24.getDocumentType(),
                "issuedAt", b24.getIssuedAt(),
                "issuedBy", b24.getIssuedBy().getFullName(),
                "identityVerified", b24.isIdentityVerified(),
                "residenceVerified", b24.isResidenceVerified());
    }

    private Map<String, Object> mapCr2Family(FormCR2FamilyInfo info) {
        if (info == null)
            return null;
        return Map.of(
                "documentType", info.getDocumentType(),
                "issuedAt", info.getIssuedAt(),
                "issuedBy", info.getIssuedBy().getFullName(),
                "cr2FormData", info.getCr2FormData());
    }

    private Map<String, Object> mapCr2(FormCR2 cr2) {
        if (cr2 == null)
            return null;
        return Map.of(
                "documentType", cr2.getDocumentType(),
                "issuedAt", cr2.getIssuedAt(),
                "issuedBy", cr2.getIssuedBy().getFullName(),
                "certificateSerialNumber", cr2.getCertificateSerialNumber());
    }
}
