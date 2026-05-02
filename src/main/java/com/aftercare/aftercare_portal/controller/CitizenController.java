package com.aftercare.aftercare_portal.controller;

import com.aftercare.aftercare_portal.dto.CitizenLookupDto;
import com.aftercare.aftercare_portal.repository.CitizenRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/citizens")
public class CitizenController {

    private final CitizenRepository citizenRepository;

    public CitizenController(CitizenRepository citizenRepository) {
        this.citizenRepository = citizenRepository;
    }

    /** Look up a citizen by NIC for death form pre-fill. Requires authentication. */
    @GetMapping("/{nic}")
    public ResponseEntity<CitizenLookupDto> lookup(@PathVariable String nic) {
        return citizenRepository.findByNic(nic)
                .map(c -> ResponseEntity.ok(CitizenLookupDto.from(c)))
                .orElse(ResponseEntity.notFound().build());
    }
}
