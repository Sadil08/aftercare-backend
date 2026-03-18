package com.aftercare.aftercare_portal.service;

import com.aftercare.aftercare_portal.dto.B24FormDto;
import com.aftercare.aftercare_portal.dto.Cr02FormDto;
import com.aftercare.aftercare_portal.entity.B24Form;
import com.aftercare.aftercare_portal.entity.Cr02Form;
import com.aftercare.aftercare_portal.entity.DeathCase;
import com.aftercare.aftercare_portal.entity.User;
import com.aftercare.aftercare_portal.enums.DeathCaseStatus;
import com.aftercare.aftercare_portal.repository.B24FormRepository;
import com.aftercare.aftercare_portal.repository.Cr02FormRepository;
import com.aftercare.aftercare_portal.repository.DeathCaseRepository;
import com.aftercare.aftercare_portal.repository.UserRepository;
import com.aftercare.aftercare_portal.dto.NotificationDTO;
import com.aftercare.aftercare_portal.dto.TrackingDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class FormService {

    private final B24FormRepository b24FormRepository;
    private final Cr02FormRepository cr02FormRepository;
    private final DeathCaseRepository deathCaseRepository;
    private final UserRepository userRepository;

    public B24Form saveB24Form(B24FormDto dto) {
        B24Form entity = B24Form.builder()
                .gramaDivision(dto.getB24GramaDivision())
                .registrarDivision(dto.getB24RegistrarDivision())
                .serialNo(dto.getB24SerialNo())
                .deathYear(dto.getB24DeathYear())
                .deathMonth(dto.getB24DeathMonth())
                .deathDay(dto.getB24DeathDay())
                .placeOfDeath(dto.getB24PlaceOfDeath())
                .fullName(dto.getB24FullName())
                .sex(dto.getB24Sex())
                .race(dto.getB24Race())
                .age(dto.getB24Age())
                .profession(dto.getB24Profession())
                .causeOfDeath(dto.getB24CauseOfDeath())
                .informantName(dto.getB24InformantName())
                .informantAddress(dto.getB24InformantAddress())
                .familyNicNo(dto.getB24FamilyNicNo())
                .assignedRegistrarUsername(dto.getAssignedRegistrarUsername())
                .submittedByUsername(dto.getSubmittedByUsername())
                .build();

        // ── Auto-fill from DeathCase if family NIC matches ──
        if (dto.getB24FamilyNicNo() != null) {
            Optional<DeathCase> caseOpt = deathCaseRepository
                    .findFirstByApplicantFamilyMember_NicNoOrderByCreatedAtDesc(dto.getB24FamilyNicNo());
            if (caseOpt.isPresent()) {
                DeathCase dc = caseOpt.get();
                // Auto-fill deceased details from the case
                if (entity.getFullName() == null || entity.getFullName().isBlank()) {
                    entity.setFullName(dc.getDeceased().getFullName());
                }
                if (entity.getSex() == null || entity.getSex().isBlank()) {
                    entity.setSex(dc.getDeceased().getGender().name());
                }
                if (entity.getPlaceOfDeath() == null || entity.getPlaceOfDeath().isBlank()) {
                    entity.setPlaceOfDeath(dc.getDeceased().getAddress());
                }
                if (entity.getDeathYear() == null) {
                    entity.setDeathYear(dc.getDeceased().getDateOfDeath().getYear());
                }
                if (entity.getDeathMonth() == null) {
                    entity.setDeathMonth(dc.getDeceased().getDateOfDeath().getMonthValue());
                }
                if (entity.getDeathDay() == null) {
                    entity.setDeathDay(dc.getDeceased().getDateOfDeath().getDayOfMonth());
                }
                // Auto-fill cause of death from B-12 (Doctor's medical certification)
                if ((entity.getCauseOfDeath() == null || entity.getCauseOfDeath().isBlank())
                        && dc.getFormB12() != null) {
                    entity.setCauseOfDeath(dc.getFormB12().getPrimaryCause());
                }
                // Auto-fill informant from applicant
                if (entity.getInformantName() == null || entity.getInformantName().isBlank()) {
                    entity.setInformantName(dc.getApplicantFamilyMember().getFullName());
                }
            }
        }

        return b24FormRepository.save(entity);
    }

    public Cr02Form saveCr02Form(Cr02FormDto dto) {
        Cr02Form entity = Cr02Form.builder()
                .typeOfDeath(dto.getTypeOfDeath())
                .deathYear(dto.getDeathYear())
                .deathMonth(dto.getDeathMonth())
                .deathDay(dto.getDeathDay())
                .district(dto.getDistrict())
                .dsDivision(dto.getDsDivision())
                .regDivision(dto.getRegDivision())
                .placeInSinhalaOrTamil(dto.getPlaceInSinhalaOrTamil())
                .placeInEnglish(dto.getPlaceInEnglish())
                .timeOfDeath(dto.getTimeOfDeath())
                .deathLocation(dto.getDeathLocation())
                .causeEstablished(dto.getCauseEstablished())
                .causeOfDeath(dto.getCauseOfDeath())
                .icdCode(dto.getIcdCode())
                .burialPlace(dto.getBurialPlace())
                .informantCapacity(dto.getInformantCapacity())
                .informantId(dto.getInformantId())
                .informantName(dto.getInformantName())
                .informantAddress(dto.getInformantAddress())
                .informantPhone(dto.getInformantPhone())
                .informantEmail(dto.getInformantEmail())
                .familyNicNo(dto.getCr02FamilyNicNo())
                .deceasedName(dto.getDeceasedName())
                .submittedByUsername(dto.getSubmittedByUsername())
                .currentStage("SUBMITTED_BY_REGISTRAR")
                .build();

        // ── Auto-fill from DeathCase if family NIC matches ──
        if (dto.getCr02FamilyNicNo() != null) {
            Optional<DeathCase> caseOpt = deathCaseRepository
                    .findFirstByApplicantFamilyMember_NicNoOrderByCreatedAtDesc(dto.getCr02FamilyNicNo());
            if (caseOpt.isPresent()) {
                DeathCase dc = caseOpt.get();
                // Auto-fill deceased name
                if (entity.getDeceasedName() == null || entity.getDeceasedName().isBlank()) {
                    entity.setDeceasedName(dc.getDeceased().getFullName());
                }
                // Auto-fill death date
                if (entity.getDeathYear() == null) {
                    entity.setDeathYear(dc.getDeceased().getDateOfDeath().getYear());
                }
                if (entity.getDeathMonth() == null) {
                    entity.setDeathMonth(dc.getDeceased().getDateOfDeath().getMonthValue());
                }
                if (entity.getDeathDay() == null) {
                    entity.setDeathDay(dc.getDeceased().getDateOfDeath().getDayOfMonth());
                }
                // Auto-fill medical info from B-12
                if (dc.getFormB12() != null) {
                    if (entity.getCauseOfDeath() == null || entity.getCauseOfDeath().isBlank()) {
                        entity.setCauseOfDeath(dc.getFormB12().getPrimaryCause());
                    }
                    if (entity.getIcdCode() == null || entity.getIcdCode().isBlank()) {
                        entity.setIcdCode(dc.getFormB12().getIcd10Code());
                    }
                }
                // Auto-fill informant from applicant
                if (entity.getInformantName() == null || entity.getInformantName().isBlank()) {
                    entity.setInformantName(dc.getApplicantFamilyMember().getFullName());
                }
                if (entity.getInformantId() == null || entity.getInformantId().isBlank()) {
                    entity.setInformantId(dc.getApplicantFamilyMember().getNicNo());
                }
                // Auto-fill district from sector
                if (entity.getDistrict() == null || entity.getDistrict().isBlank()) {
                    entity.setDistrict(dc.getSector().getDistrict());
                }
            }
        }

        Cr02Form saved = cr02FormRepository.save(entity);

        // Mark matching B24 forms as COMPLETED
        if (dto.getCr02FamilyNicNo() != null) {
            List<B24Form> b24Forms = b24FormRepository.findByFamilyNicNo(dto.getCr02FamilyNicNo());
            for (B24Form b24 : b24Forms) {
                b24.setCurrentStage("COMPLETED");
                b24FormRepository.save(b24);
            }
        }

        return saved;
    }

    public B24Form getB24FormById(Long id) {
        return b24FormRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("B24 form not found with id: " + id));
    }

    public NotificationDTO getUnreadNotifications(String username, String nicNo, String role) {
        long count = 0;
        List<String> messages = new ArrayList<>();
        List<Long> formIds = new ArrayList<>();

        Optional<User> userOpt = Optional.empty();
        if (username != null) {
            userOpt = userRepository.findByUsername(username);
        }

        if ("REGISTRAR".equalsIgnoreCase(role)) {
            // Standalone Forms
            List<B24Form> pendingForms = b24FormRepository
                    .findByAssignedRegistrarUsernameAndCurrentStage(username, "SUBMITTED_BY_GN");
            for (B24Form form : pendingForms) {
                formIds.add(form.getId());
                messages.add("Standalone B24 Report for " + (form.getFullName() != null ? form.getFullName() : "Unknown")
                        + " is pending review.");
            }
            count += pendingForms.size();

            // DeathCase Workflow
            long dcCount = deathCaseRepository.countByStatus(DeathCaseStatus.PENDING_REGISTRAR_REVIEW);
            if (dcCount > 0) {
                messages.add(dcCount + " cases are pending final Registrar review (B-2 Issuance).");
                count += dcCount;
            }

        } else if ("DOCTOR".equalsIgnoreCase(role)) {
            // DeathCase Workflow
            long dcCount = deathCaseRepository.countByStatus(DeathCaseStatus.PENDING_B12_MEDICAL);
            if (dcCount > 0) {
                messages.add(dcCount + " cases are pending Medical Certification (B-12).");
                count += dcCount;
            }

        } else if ("FAMILY".equalsIgnoreCase(role) && nicNo != null) {
            // Standalone Forms
            long b24Count = b24FormRepository.countByCurrentStageAndFamilyNicNo("APPROVED", nicNo);
            long cr02Count = cr02FormRepository.countByCurrentStageAndFamilyNicNo("APPROVED", nicNo);
            if (b24Count + cr02Count > 0) {
                messages.add("You have " + (b24Count + cr02Count) + " standalone forms approved and ready.");
                count += (b24Count + cr02Count);
            }

            // DeathCase Workflow
            if (userOpt.isPresent()) {
                long pendingCr2FamilyCount = deathCaseRepository.countByApplicantFamilyMemberAndStatus(userOpt.get(), DeathCaseStatus.PENDING_CR2_FAMILY);
                if (pendingCr2FamilyCount > 0) {
                    messages.add("You have " + pendingCr2FamilyCount + " cases pending your CR-2 Form Declaration.");
                    count += pendingCr2FamilyCount;
                }
                long closedCount = deathCaseRepository.countByApplicantFamilyMemberAndStatus(userOpt.get(), DeathCaseStatus.CR2_ISSUED_CLOSED);
                if (closedCount > 0) {
                    messages.add("You have " + closedCount + " fully completed cases (CR-2 Certificate Issued).");
                    // We don't strictly *have* to add closed to unread count, but keeping logic consistent
                    count += closedCount;
                }
            }

        } else if ("GRAMA_NILADHARI".equalsIgnoreCase(role)) {
            // Standalone Forms
            long reviewedCount = b24FormRepository.countByCurrentStage("REVIEW_BY_REGISTRAR");
            long approvedCount = b24FormRepository.countByCurrentStage("APPROVED");
            if (reviewedCount > 0) {
                messages.add(reviewedCount + " standalone B24 reports are under registrar review.");
                count += reviewedCount;
            }
            if (approvedCount > 0) {
                messages.add(approvedCount + " standalone B24 reports have been approved.");
                count += approvedCount;
            }

            // DeathCase Workflow
            if (userOpt.isPresent() && userOpt.get().getSector() != null) {
                long dcCount = deathCaseRepository.countByStatusAndSector(DeathCaseStatus.PENDING_B24_GN, userOpt.get().getSector());
                if (dcCount > 0) {
                    messages.add(dcCount + " cases are pending your Identity/Residence Verification (B-24).");
                    count += dcCount;
                }
            }
        }

        return NotificationDTO.builder()
                .unreadCount((int) count)
                .messages(messages)
                .formIds(formIds)
                .build();
    }

    public List<TrackingDTO> getTrackingInfo(String familyNicNo) {
        List<TrackingDTO> trackingList = new ArrayList<>();

        List<B24Form> b24Forms = b24FormRepository.findByFamilyNicNo(familyNicNo);
        for (B24Form form : b24Forms) {
            trackingList.add(TrackingDTO.builder()
                    .formId(form.getId())
                    .formType("B24")
                    .currentStage(form.getCurrentStage())
                    .updatedAt(form.getUpdatedAt() != null ? form.getUpdatedAt() : form.getCreatedAt())
                    .submittedAt(form.getSubmissionTimestamp() != null ? form.getSubmissionTimestamp()
                            : form.getCreatedAt())
                    .record("B24 Report")
                    .deceasedName(form.getFullName())
                    .build());
        }

        List<Cr02Form> cr02Forms = cr02FormRepository.findByFamilyNicNo(familyNicNo);
        for (Cr02Form form : cr02Forms) {
            trackingList.add(TrackingDTO.builder()
                    .formId(form.getId())
                    .formType("CR02")
                    .currentStage(form.getCurrentStage())
                    .updatedAt(form.getUpdatedAt() != null ? form.getUpdatedAt() : form.getCreatedAt())
                    .submittedAt(form.getSubmissionTimestamp() != null ? form.getSubmissionTimestamp()
                            : form.getCreatedAt())
                    .record("CR02 Declaration")
                    .deceasedName(form.getDeceasedName())
                    .build());
        }

        trackingList.sort(Comparator.comparing(TrackingDTO::getUpdatedAt).reversed());
        return trackingList;
    }

    public Cr02Form getCr02FormById(Long id) {
        return cr02FormRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CR02 form not found with id: " + id));
    }

    public List<TrackingDTO> getGnHistory(String username) {
        List<TrackingDTO> result = new ArrayList<>();
        List<B24Form> forms = b24FormRepository.findBySubmittedByUsername(username);
        for (B24Form form : forms) {
            result.add(TrackingDTO.builder()
                    .formId(form.getId())
                    .formType("B24")
                    .currentStage(form.getCurrentStage())
                    .updatedAt(form.getUpdatedAt() != null ? form.getUpdatedAt() : form.getCreatedAt())
                    .submittedAt(form.getSubmissionTimestamp() != null ? form.getSubmissionTimestamp()
                            : form.getCreatedAt())
                    .record("B24 Report")
                    .build());
        }
        result.sort(Comparator.comparing(TrackingDTO::getSubmittedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return result;
    }

    public List<TrackingDTO> getRegistrarHistory(String username) {
        List<TrackingDTO> result = new ArrayList<>();
        List<Cr02Form> forms = cr02FormRepository.findBySubmittedByUsername(username);
        for (Cr02Form form : forms) {
            result.add(TrackingDTO.builder()
                    .formId(form.getId())
                    .formType("CR02")
                    .currentStage(form.getCurrentStage())
                    .updatedAt(form.getUpdatedAt() != null ? form.getUpdatedAt() : form.getCreatedAt())
                    .submittedAt(form.getSubmissionTimestamp() != null ? form.getSubmissionTimestamp()
                            : form.getCreatedAt())
                    .record("CR02 Declaration")
                    .build());
        }
        result.sort(Comparator.comparing(TrackingDTO::getSubmittedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return result;
    }
}
