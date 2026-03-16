package com.aftercare.aftercare_portal.controller;

import com.aftercare.aftercare_portal.dto.TrackingDTO;
import com.aftercare.aftercare_portal.service.FormService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final FormService formService;

    @GetMapping("/{familyUserId}")
    public ResponseEntity<List<TrackingDTO>> getTrackingInfo(@PathVariable Long familyUserId) {
        List<TrackingDTO> trackingInfo = formService.getTrackingInfo(familyUserId);
        return ResponseEntity.ok(trackingInfo);
    }
}
