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
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
public class HistoryController {

    private final FormService formService;

    @GetMapping("/gn/{username}")
    public ResponseEntity<List<TrackingDTO>> getGnHistory(@PathVariable String username) {
        return ResponseEntity.ok(formService.getGnHistory(username));
    }

    @GetMapping("/registrar/{username}")
    public ResponseEntity<List<TrackingDTO>> getRegistrarHistory(@PathVariable String username) {
        return ResponseEntity.ok(formService.getRegistrarHistory(username));
    }
}
