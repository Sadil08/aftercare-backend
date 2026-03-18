package com.aftercare.aftercare_portal.service;

import com.aftercare.aftercare_portal.dto.CemeteryRequestDto;
import com.aftercare.aftercare_portal.entity.CemeteryRequest;
import com.aftercare.aftercare_portal.repository.CemeteryRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CemeteryRequestService {

    private final CemeteryRequestRepository repository;

    public CemeteryRequestDto createRequest(CemeteryRequestDto dto) {
        if (repository.existsByCr02FormId(dto.getCr02FormId())) {
            throw new RuntimeException("A cemetery request already exists for this CR02 form.");
        }

        CemeteryRequest entity = CemeteryRequest.builder()
                .familyNicNo(dto.getFamilyNicNo())
                .cr02FormId(dto.getCr02FormId())
                .deceasedName(dto.getDeceasedName())
                .cemeteryUsername(dto.getCemeteryUsername())
                .requestedDate(dto.getRequestedDate())
                .status("PENDING")
                .timeSlot(dto.getTimeSlot())
                .build();

        CemeteryRequest saved = repository.save(entity);
        return mapToDto(saved);
    }

    public List<CemeteryRequestDto> getRequestsForFamily(String nicNo) {
        return repository.findByFamilyNicNoOrderByCreatedAtDesc(nicNo).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<CemeteryRequestDto> getRequestsForCemetery(String username) {
        return repository.findByCemeteryUsernameOrderByCreatedAtDesc(username).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<String> getBookedSlots(String username, String date) {
        return repository.findByCemeteryUsernameAndRequestedDateAndStatusIn(
                username, date, List.of("PENDING", "APPROVED")
        ).stream().map(CemeteryRequest::getTimeSlot).collect(Collectors.toList());
    }

    public CemeteryRequestDto updateStatus(Long id, String status) {
        CemeteryRequest entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cemetery request not found"));

        entity.setStatus(status);

        CemeteryRequest updated = repository.save(entity);
        return mapToDto(updated);
    }

    private CemeteryRequestDto mapToDto(CemeteryRequest entity) {
        return CemeteryRequestDto.builder()
                .id(entity.getId())
                .familyNicNo(entity.getFamilyNicNo())
                .cr02FormId(entity.getCr02FormId())
                .deceasedName(entity.getDeceasedName())
                .cemeteryUsername(entity.getCemeteryUsername())
                .requestedDate(entity.getRequestedDate())
                .status(entity.getStatus())
                .timeSlot(entity.getTimeSlot())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
