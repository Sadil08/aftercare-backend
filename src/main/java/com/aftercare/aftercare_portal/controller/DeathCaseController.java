package com.aftercare.aftercare_portal.controller;

import com.aftercare.aftercare_portal.dto.*;
import com.aftercare.aftercare_portal.entity.User;
import com.aftercare.aftercare_portal.enums.DeathCaseStatus;
import com.aftercare.aftercare_portal.repository.UserRepository;
import com.aftercare.aftercare_portal.service.DeathCaseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cases")
public class DeathCaseController {

    private final DeathCaseService deathCaseService;
    private final UserRepository userRepository;

    public DeathCaseController(DeathCaseService deathCaseService, UserRepository userRepository) {
        this.deathCaseService = deathCaseService;
        this.userRepository = userRepository;
    }

    // ──── F01: Family Initiates Case (now includes CR-2 data + optional doctorId) ────
    @PostMapping
    public ResponseEntity<CaseResponse> createCase(@Valid @RequestBody CreateCaseRequest request, Authentication auth) {
        User user = getUser(auth);
        CaseResponse response = deathCaseService.createCase(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ──── F02: GN Review Action (Approve or Request Medical) ────
    @PostMapping("/{caseId}/gn-action")
    public ResponseEntity<CaseResponse> gnAction(
            @PathVariable(name = "caseId") Long caseId,
            @Valid @RequestBody GnActionRequest request,
            Authentication auth) {
        User gn = getUser(auth);
        CaseResponse response = deathCaseService.gnAction(caseId, gn, request);
        return ResponseEntity.ok(response);
    }

    // ──── F03: Family Assigns Doctor (fallback when PENDING_DOCTOR_ASSIGNMENT) ────
    @PatchMapping("/{caseId}/assign-doctor")
    public ResponseEntity<CaseResponse> assignDoctor(
            @PathVariable(name = "caseId") Long caseId,
            @Valid @RequestBody AssignDoctorRequest request,
            Authentication auth) {
        User family = getUser(auth);
        CaseResponse response = deathCaseService.assignDoctor(caseId, family, request);
        return ResponseEntity.ok(response);
    }

    // ──── F04: Doctor Issues B-12 (Medical Certification) ────
    @PostMapping("/{caseId}/b12")
    public ResponseEntity<CaseResponse> issueB12(
            @PathVariable(name = "caseId") Long caseId,
            @Valid @RequestBody IssueB12Request request,
            Authentication auth) {
        User doctor = getUser(auth);
        CaseResponse response = deathCaseService.issueB12(caseId, doctor, request);
        return ResponseEntity.ok(response);
    }

    // ──── F05: Registrar Issues CR-2 (pre-filled from creation-time data) ────
    @PostMapping("/{caseId}/cr2")
    public ResponseEntity<CaseResponse> issueCr2(
            @PathVariable(name = "caseId") Long caseId,
            Authentication auth) {
        User registrar = getUser(auth);
        CaseResponse response = deathCaseService.issueCr2(caseId, registrar);
        return ResponseEntity.ok(response);
    }

    // ──── Case Listing & Tracking ────

    @GetMapping
    public ResponseEntity<Page<CaseListResponse>> getCases(
            @RequestParam(name = "status", required = false) DeathCaseStatus status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            Authentication auth) {

        User user = getUser(auth);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CaseListResponse> responses = deathCaseService.getCasesForUser(user, status, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{caseId}")
    public ResponseEntity<CaseResponse> getCaseDetail(
            @PathVariable(name = "caseId") Long caseId,
            Authentication auth) {
        User user = getUser(auth);
        CaseResponse response = deathCaseService.getCaseDetail(caseId, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active/{familyNic}")
    public ResponseEntity<CaseResponse> getActiveCaseByFamilyNic(
            @PathVariable(name = "familyNic") String familyNic) {
        try {
            CaseResponse response = deathCaseService.getActiveCaseByFamilyNic(familyNic);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Helper
    private User getUser(Authentication auth) {
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new SecurityException("Authenticated user not found."));
    }
}
