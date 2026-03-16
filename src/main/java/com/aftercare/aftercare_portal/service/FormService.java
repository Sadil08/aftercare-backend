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
                .build();

        return cr02FormRepository.save(entity);
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
                    .deceasedName("CR02 Record")
                    .build());
        }

        trackingList.sort(Comparator.comparing(TrackingDTO::getUpdatedAt).reversed());
        return trackingList;
    }
}
