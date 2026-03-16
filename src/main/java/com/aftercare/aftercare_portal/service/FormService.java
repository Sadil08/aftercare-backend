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
                .familyUserId(dto.getB24FamilyUserId())
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
                .familyUserId(dto.getCr02FamilyUserId())
                .build();
                
        return cr02FormRepository.save(entity);
    }

    public NotificationDTO getUnreadNotifications(Long userId, String role) {
        long count = 0;
        List<String> messages = new ArrayList<>();

        if ("REGISTRAR".equalsIgnoreCase(role)) {
            long b24Count = b24FormRepository.countByCurrentStage("SUBMITTED_BY_GN");
            long cr02Count = cr02FormRepository.countByCurrentStage("SUBMITTED_BY_GN");
            count = b24Count + cr02Count;
            if (count > 0) {
                messages.add("You have " + count + " new forms pending review.");
            }
        } else if ("FAMILY".equalsIgnoreCase(role)) {
            long b24Count = b24FormRepository.countByCurrentStageAndFamilyUserId("APPROVED", userId);
            long cr02Count = cr02FormRepository.countByCurrentStageAndFamilyUserId("APPROVED", userId);
            count = b24Count + cr02Count;
            if (count > 0) {
                messages.add("You have " + count + " forms approved and ready.");
            }
        }
        
        return NotificationDTO.builder()
                .unreadCount((int) count)
                .messages(messages)
                .build();
    }

    public List<TrackingDTO> getTrackingInfo(Long familyUserId) {
        List<TrackingDTO> trackingList = new ArrayList<>();
        
        List<B24Form> b24Forms = b24FormRepository.findByFamilyUserId(familyUserId);
        for (B24Form form : b24Forms) {
            trackingList.add(TrackingDTO.builder()
                    .formId(form.getId())
                    .formType("B24")
                    .currentStage(form.getCurrentStage())
                    .updatedAt(form.getUpdatedAt() != null ? form.getUpdatedAt() : form.getCreatedAt())
                    .submittedAt(form.getCreatedAt())
                    .deceasedName(form.getFullName())
                    .build());
        }

        List<Cr02Form> cr02Forms = cr02FormRepository.findByFamilyUserId(familyUserId);
        for (Cr02Form form : cr02Forms) {
            trackingList.add(TrackingDTO.builder()
                    .formId(form.getId())
                    .formType("CR02")
                    .currentStage(form.getCurrentStage())
                    .updatedAt(form.getUpdatedAt() != null ? form.getUpdatedAt() : form.getCreatedAt())
                    .submittedAt(form.getCreatedAt())
                    .deceasedName("CR02 Record") 
                    .build());
        }
        
        trackingList.sort(Comparator.comparing(TrackingDTO::getUpdatedAt).reversed());
        return trackingList;
    }
}
