package com.aftercare.aftercare_portal.service;

import com.aftercare.aftercare_portal.dto.B24FormDto;
import com.aftercare.aftercare_portal.dto.Cr02FormDto;
import com.aftercare.aftercare_portal.entity.B24Form;
import com.aftercare.aftercare_portal.entity.Cr02Form;
import com.aftercare.aftercare_portal.repository.B24FormRepository;
import com.aftercare.aftercare_portal.repository.Cr02FormRepository;
import com.aftercare.aftercare_portal.dto.NotificationDTO;
import com.aftercare.aftercare_portal.dto.TrackingDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Service
@RequiredArgsConstructor
public class FormService {

    private final B24FormRepository b24FormRepository;
    private final Cr02FormRepository cr02FormRepository;

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

        if ("REGISTRAR".equalsIgnoreCase(role)) {
            // Registrar sees B24 forms assigned to them
            List<B24Form> pendingForms = b24FormRepository
                    .findByAssignedRegistrarUsernameAndCurrentStage(username, "SUBMITTED_BY_GN");
            count = pendingForms.size();
            for (B24Form form : pendingForms) {
                formIds.add(form.getId());
                messages.add("B24 Report for " + (form.getFullName() != null ? form.getFullName() : "Unknown")
                        + " is pending review.");
            }
        } else if ("FAMILY".equalsIgnoreCase(role) && nicNo != null) {
            long b24Count = b24FormRepository.countByCurrentStageAndFamilyNicNo("APPROVED", nicNo);
            long cr02Count = cr02FormRepository.countByCurrentStageAndFamilyNicNo("APPROVED", nicNo);
            count = b24Count + cr02Count;
            if (count > 0) {
                messages.add("You have " + count + " forms approved and ready.");
            }
        } else if ("GRAMA_NILADHARI".equalsIgnoreCase(role) || "GN".equalsIgnoreCase(role)) {
            // GN can see how many of their submitted B24 forms have been reviewed
            long reviewedCount = b24FormRepository.countByCurrentStage("REVIEW_BY_REGISTRAR");
            long approvedCount = b24FormRepository.countByCurrentStage("APPROVED");
            count = reviewedCount + approvedCount;
            if (reviewedCount > 0) {
                messages.add(reviewedCount + " B24 reports are under registrar review.");
            }
            if (approvedCount > 0) {
                messages.add(approvedCount + " B24 reports have been approved.");
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
