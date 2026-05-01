package com.aftercare.aftercare_portal.controller;

import com.aftercare.aftercare_portal.dto.AssignDoctorRequest;
import com.aftercare.aftercare_portal.dto.CaseListResponse;
import com.aftercare.aftercare_portal.dto.CaseResponse;
import com.aftercare.aftercare_portal.dto.CreateCaseRequest;
import com.aftercare.aftercare_portal.dto.GnActionRequest;
import com.aftercare.aftercare_portal.dto.IssueB12Request;
import com.aftercare.aftercare_portal.dto.IssueB24Request;
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

    @PostMapping
    public ResponseEntity<CaseResponse> createCase(@Valid @RequestBody CreateCaseRequest request, Authentication auth) {
        User user = getUser(auth);
        CaseResponse response = deathCaseService.createCase(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{caseId}/gn-action")
    public ResponseEntity<CaseResponse> gnAction(
            @PathVariable(name = "caseId") Long caseId,
            @Valid @RequestBody GnActionRequest request,
            Authentication auth) {
        User gn = getUser(auth);
        CaseResponse response = deathCaseService.gnAction(caseId, gn, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{caseId}/assign-doctor")
    public ResponseEntity<CaseResponse> assignDoctor(
            @PathVariable(name = "caseId") Long caseId,
            @Valid @RequestBody AssignDoctorRequest request,
            Authentication auth) {
        User family = getUser(auth);
        CaseResponse response = deathCaseService.assignDoctor(caseId, family, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{caseId}/b12")
    public ResponseEntity<CaseResponse> issueB12(
            @PathVariable(name = "caseId") Long caseId,
            @Valid @RequestBody IssueB12Request request,
            Authentication auth) {
        User doctor = getUser(auth);
        CaseResponse response = deathCaseService.issueB12(caseId, doctor, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{caseId}/b24")
    public ResponseEntity<CaseResponse> issueB24(
            @PathVariable(name = "caseId") Long caseId,
            @Valid @RequestBody IssueB24Request request,
            Authentication auth) {
        User gn = getUser(auth);
        CaseResponse response = deathCaseService.issueB24(caseId, gn, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{caseId}/cr2")
    public ResponseEntity<CaseResponse> issueCr2(
            @PathVariable(name = "caseId") Long caseId,
            Authentication auth) {
        User registrar = getUser(auth);
        CaseResponse response = deathCaseService.issueCr2(caseId, registrar);
        return ResponseEntity.ok(response);
    }

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

    @DeleteMapping("/{caseId}")
    public ResponseEntity<Void> deleteCase(
            @PathVariable(name = "caseId") Long caseId,
            Authentication auth) {
        User user = getUser(auth);
        deathCaseService.deleteCase(caseId, user);
        return ResponseEntity.noContent().build();
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

    private User getUser(Authentication auth) {
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new SecurityException("Authenticated user not found."));
    }
}
