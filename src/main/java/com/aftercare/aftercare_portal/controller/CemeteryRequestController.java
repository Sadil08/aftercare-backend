package com.aftercare.aftercare_portal.controller;

import com.aftercare.aftercare_portal.dto.CemeteryRequestDto;
import com.aftercare.aftercare_portal.service.CemeteryRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cemetery-requests")
@RequiredArgsConstructor
public class CemeteryRequestController {

    private final CemeteryRequestService service;

    @PostMapping
    public ResponseEntity<CemeteryRequestDto> createRequest(@RequestBody CemeteryRequestDto request) {
        try {
            return ResponseEntity.ok(service.createRequest(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/family/{nicNo}")
    public ResponseEntity<List<CemeteryRequestDto>> getRequestsForFamily(@PathVariable String nicNo) {
        return ResponseEntity.ok(service.getRequestsForFamily(nicNo));
    }

    @GetMapping("/cemetery/{username}")
    public ResponseEntity<List<CemeteryRequestDto>> getRequestsForCemetery(@PathVariable String username) {
        return ResponseEntity.ok(service.getRequestsForCemetery(username));
    }

    @GetMapping("/cemetery/{username}/booked-slots")
    public ResponseEntity<List<String>> getBookedSlots(@PathVariable String username, @RequestParam String date) {
        return ResponseEntity.ok(service.getBookedSlots(username, date));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CemeteryRequestDto> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            return ResponseEntity.ok(service.updateStatus(id, status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
