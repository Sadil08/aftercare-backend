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

    // ──── F03: Initiate Case ────
    @PostMapping
    public ResponseEntity<CaseResponse> createCase(@Valid @RequestBody CreateCaseRequest request, Authentication auth) {
        User user = getUser(auth);
        CaseResponse response = deathCaseService.createCase(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ──── F04: Issue B-24 ────
    @PostMapping("/{caseId}/b24")
    public ResponseEntity<CaseResponse> issueB24(@PathVariable Long caseId, @Valid @RequestBody IssueB24Request request,
            Authentication auth) {
        User GN = getUser(auth);
        CaseResponse response = deathCaseService.issueB24(caseId, GN, request);
        return ResponseEntity.ok(response);
    }

    // ──── F05: Issue B-12 ────
    @PostMapping("/{caseId}/b12")
    public ResponseEntity<CaseResponse> issueB12(@PathVariable Long caseId, @Valid @RequestBody IssueB12Request request,
            Authentication auth) {
        User doctor = getUser(auth);
        CaseResponse response = deathCaseService.issueB12(caseId, doctor, request);
        return ResponseEntity.ok(response);
    }

    // ──── F06: Submit B-11 ────
    @PostMapping("/{caseId}/b11")
    public ResponseEntity<CaseResponse> submitB11(@PathVariable Long caseId,
            @Valid @RequestBody SubmitB11Request request, Authentication auth) {
        User applicant = getUser(auth);
        CaseResponse response = deathCaseService.submitB11(caseId, applicant, request);
        return ResponseEntity.ok(response);
    }

    // ──── F07: Issue B-2 ────
    @PostMapping("/{caseId}/b2")
    public ResponseEntity<CaseResponse> issueB2(@PathVariable Long caseId, Authentication auth) {
        User registrar = getUser(auth);
        CaseResponse response = deathCaseService.issueB2(caseId, registrar);
        return ResponseEntity.ok(response);
    }

    // ──── F08: Case Tracking ────

    @GetMapping
    public ResponseEntity<Page<CaseListResponse>> getCases(
            @RequestParam(required = false) DeathCaseStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {

        User user = getUser(auth);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CaseListResponse> responses = deathCaseService.getCasesForUser(user, status, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{caseId}")
    public ResponseEntity<CaseResponse> getCaseDetail(@PathVariable Long caseId, Authentication auth) {
        User user = getUser(auth);
        CaseResponse response = deathCaseService.getCaseDetail(caseId, user);
        return ResponseEntity.ok(response);
    }

    // Helper
    private User getUser(Authentication auth) {
        String nic = auth.getName();
        return userRepository.findByNicNo(nic)
                .orElseThrow(() -> new SecurityException("Authenticated user not found."));
    }
}
