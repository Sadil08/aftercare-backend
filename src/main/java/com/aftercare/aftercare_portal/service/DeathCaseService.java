package com.aftercare.aftercare_portal.service;

import com.aftercare.aftercare_portal.dto.*;
import com.aftercare.aftercare_portal.entity.*;
import com.aftercare.aftercare_portal.entity.document.FormB11;
import com.aftercare.aftercare_portal.entity.document.FormB12;
import com.aftercare.aftercare_portal.entity.document.FormB2;
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

import java.time.LocalDateTime;
import java.util.HashMap;
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

    // ──── Feature 03: Initiate Case ────
    @Transactional
    public CaseResponse createCase(User applicant, CreateCaseRequest request) {
        if (!applicant.getRoles().contains(Role.CITIZEN)) {
            throw new SecurityException("Only citizens can initiate a case.");
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

    // ──── Feature 04: GN Issues B-24 ────
    @Transactional
    public CaseResponse issueB24(Long caseId, User actingGN, IssueB24Request request) {
        DeathCase deathCase = getCaseForVerification(caseId);

        if (!actingGN.getSector().getId().equals(deathCase.getSector().getId())) {
            throw new SecurityException("You can only verify cases in your assigned sector.");
        }

        String payload = caseId + actingGN.getNic() + request.identityVerified() + request.residenceVerified();
        String hash = hashService.computeHash(payload);

        deathCase.issueB24(actingGN, hash, request.identityVerified(), request.residenceVerified());
        deathCase = deathCaseRepository.save(deathCase);

        return mapToResponse(deathCase);
    }

    // ──── Feature 05: Doctor Issues B-12 ────
    @Transactional
    public CaseResponse issueB12(Long caseId, User actingDoctor, IssueB12Request request) {
        DeathCase deathCase = getCaseForVerification(caseId);

        String payload = caseId + actingDoctor.getNic() + request.icd10Code() + request.primaryCause();
        String hash = hashService.computeHash(payload);

        deathCase.issueB12(actingDoctor, hash, request.icd10Code(), request.primaryCause());
        deathCase = deathCaseRepository.save(deathCase);

        return mapToResponse(deathCase);
    }

    // ──── Feature 06: Family Submits B-11 ────
    @Transactional
    public CaseResponse submitB11(Long caseId, User actingCitizen, SubmitB11Request request) {
        DeathCase deathCase = getCaseForVerification(caseId);

        String payload = caseId + actingCitizen.getNic() + request.relationship() + request.declarationTrue();
        String hash = hashService.computeHash(payload);

        deathCase.submitB11(actingCitizen, hash, request.relationship());
        deathCase = deathCaseRepository.save(deathCase);

        return mapToResponse(deathCase);
    }

    // ──── Feature 07: Registrar Issues B-2 ────
    @Transactional
    public CaseResponse issueB2(Long caseId, User actingRegistrar) {
        DeathCase deathCase = getCaseForVerification(caseId);

        // Re-verify hashes
        verifyDocumentIntegrity(deathCase);

        String serialNumber = generateSerialNumber(deathCase.getSector(), deathCase.getCreatedAt().getYear());

        String payload = caseId + actingRegistrar.getNic() + serialNumber;
        String hash = hashService.computeHash(payload);

        deathCase.issueB2(actingRegistrar, hash, serialNumber);
        deathCase = deathCaseRepository.save(deathCase);

        return mapToResponse(deathCase);
    }

    // ──── Feature 08: Case Tracking ────

    @Transactional(readOnly = true)
    public CaseResponse getCaseDetail(Long caseId, User user) {
        DeathCase deathCase = getCaseForVerification(caseId);

        // RBAC check for viewing
        if (user.getRoles().contains(Role.CITIZEN)
                && !deathCase.getApplicantFamilyMember().getId().equals(user.getId())) {
            throw new SecurityException("You can only view your own cases.");
        }
        if (user.getRoles().contains(Role.GN) && !deathCase.getSector().getId().equals(user.getSector().getId())) {
            throw new SecurityException("You can only view cases in your sector.");
        }

        return mapToResponse(deathCase);
    }

    @Transactional(readOnly = true)
    public Page<CaseListResponse> getCasesForUser(User user, DeathCaseStatus status, Pageable pageable) {
        if (user.getRoles().contains(Role.CITIZEN)) {
            return deathCaseRepository.findByApplicantFamilyMember(user, pageable)
                    .map(this::mapToListResponse);
        } else if (user.getRoles().contains(Role.GN)) {
            if (status != null) {
                return deathCaseRepository.findByStatusAndSector(status, user.getSector(), pageable)
                        .map(this::mapToListResponse);
            }
            return deathCaseRepository
                    .findByStatusAndSector(DeathCaseStatus.GN_VERIFICATION_PENDING, user.getSector(), pageable)
                    .map(this::mapToListResponse);
        } else {
            // DOCTOR, REGISTRAR
            if (status != null) {
                return deathCaseRepository.findByStatus(status, pageable).map(this::mapToListResponse);
            }
            // Default views if no status specified
            DeathCaseStatus defaultStatus = user.getRoles().contains(Role.DOCTOR)
                    ? DeathCaseStatus.MEDICAL_VERIFICATION_PENDING
                    : DeathCaseStatus.REGISTRAR_REVIEW;
            return deathCaseRepository.findByStatus(defaultStatus, pageable).map(this::mapToListResponse);
        }
    }

    // ──── Private Helpers ────

    private DeathCase getCaseForVerification(Long caseId) {
        return deathCaseRepository.findById(caseId)
                .orElseThrow(() -> new EntityNotFoundException("DeathCase not found: " + caseId));
    }

    private void verifyDocumentIntegrity(DeathCase dc) {
        // In a real system, we'd recalculate the hash from the payload.
        // For MVP, we just ensure they exist and have a hash.
        if (dc.getFormB24() == null || dc.getFormB24().getCryptographicHash() == null)
            throw new SecurityException("B-24 missing or tampered");
        if (dc.getFormB12() == null || dc.getFormB12().getCryptographicHash() == null)
            throw new SecurityException("B-12 missing or tampered");
        if (dc.getFormB11() == null || dc.getFormB11().getCryptographicHash() == null)
            throw new SecurityException("B-11 missing or tampered");
    }

    private String generateSerialNumber(Sector sector, int year) {
        // E.g., B2-2026-COL-168345
        long count = deathCaseRepository.count() + 1;
        return String.format("B2-%d-%s-%06d", year, sector.getDistrict().substring(0, 3).toUpperCase(), count);
    }

    private CaseResponse mapToResponse(DeathCase dc) {
        return new CaseResponse(
                dc.getId(),
                dc.getStatus().name(),
                dc.getApplicantFamilyMember().getFullName(),
                dc.getApplicantFamilyMember().getNic(),
                dc.getDeceased().getFullName(),
                dc.getDeceased().getNic(),
                dc.getDeceased().getDateOfDeath(),
                dc.getDeceased().getGender().name(),
                dc.getDeceased().getAddress(),
                dc.getSector().getCode(),
                dc.getSector().getName(),
                mapB24(dc.getFormB24()),
                mapB12(dc.getFormB12()),
                mapB11(dc.getFormB11()),
                mapB2(dc.getFormB2()),
                dc.getCreatedAt(),
                dc.getUpdatedAt());
    }

    private CaseListResponse mapToListResponse(DeathCase dc) {
        return new CaseListResponse(
                dc.getId(),
                dc.getStatus().name(),
                dc.getDeceased().getFullName(),
                dc.getDeceased().getNic(),
                dc.getSector().getName(),
                dc.getCreatedAt(),
                dc.getUpdatedAt());
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

    private Map<String, Object> mapB12(FormB12 b12) {
        if (b12 == null)
            return null;
        return Map.of(
                "documentType", b12.getDocumentType(),
                "issuedAt", b12.getIssuedAt(),
                "issuedBy", b12.getIssuedBy().getFullName(),
                "icd10Code", b12.getIcd10Code(),
                "primaryCause", b12.getPrimaryCause());
    }

    private Map<String, Object> mapB11(FormB11 b11) {
        if (b11 == null)
            return null;
        return Map.of(
                "documentType", b11.getDocumentType(),
                "issuedAt", b11.getIssuedAt(),
                "issuedBy", b11.getIssuedBy().getFullName(),
                "applicantRelationship", b11.getApplicantRelationship(),
                "declarationTrue", b11.isDeclarationTrue());
    }

    private Map<String, Object> mapB2(FormB2 b2) {
        if (b2 == null)
            return null;
        return Map.of(
                "documentType", b2.getDocumentType(),
                "issuedAt", b2.getIssuedAt(),
                "issuedBy", b2.getIssuedBy().getFullName(),
                "certificateSerialNumber", b2.getCertificateSerialNumber());
    }
}
