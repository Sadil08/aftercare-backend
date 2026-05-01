package com.aftercare.aftercare_portal.controller;

import com.aftercare.aftercare_portal.entity.DeathCase;
import com.aftercare.aftercare_portal.entity.document.FormCR2;
import com.aftercare.aftercare_portal.repository.DeathCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/verify")
@RequiredArgsConstructor
public class VerificationController {

    private final DeathCaseRepository deathCaseRepository;

    @GetMapping("/cr2/{serialNumber}")
    public ResponseEntity<Map<String, Object>> verifyCr2(@PathVariable String serialNumber) {
        return deathCaseRepository.findByFormCr2_CertificateSerialNumber(serialNumber)
                .map(dc -> {
                    FormCR2 cr2 = dc.getFormCr2();
                    return ResponseEntity.ok(Map.<String, Object>of(
                            "valid", true,
                            "serialNumber", cr2.getCertificateSerialNumber(),
                            "deceasedName", dc.getDeceased().getDisplayFullName(),
                            "dateOfDeath", dc.getDeceased().getDateOfDeath(),
                            "issuedAt", cr2.getIssuedAt(),
                            "registrarName", cr2.getIssuedBy().getFullName(),
                            "caseId", dc.getId()));
                })
                .orElseGet(() -> ResponseEntity.notFound().<Map<String, Object>>build());
    }
}
