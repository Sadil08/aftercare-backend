package com.aftercare.aftercare_portal.controller;

import com.aftercare.aftercare_portal.dto.B24FormDto;
import com.aftercare.aftercare_portal.dto.Cr02FormDto;
import com.aftercare.aftercare_portal.entity.B24Form;
import com.aftercare.aftercare_portal.entity.Cr02Form;
import com.aftercare.aftercare_portal.service.FormService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FormController {

    private final FormService formService;

    @PostMapping("/b24")
    public ResponseEntity<B24Form> submitB24(@RequestBody B24FormDto request) {
        B24Form savedForm = formService.saveB24Form(request);
        return ResponseEntity.ok(savedForm);
    }

    @GetMapping("/b24/{id}")
    public ResponseEntity<?> getB24ById(@PathVariable Long id) {
        try {
            B24Form form = formService.getB24FormById(id);
            return ResponseEntity.ok(form);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/cr02")
    public ResponseEntity<Cr02Form> submitCr02(@RequestBody Cr02FormDto request) {
        Cr02Form savedForm = formService.saveCr02Form(request);
        return ResponseEntity.ok(savedForm);
    }

    @GetMapping("/cr02/{id}")
    public ResponseEntity<?> getCr02ById(@PathVariable Long id) {
        try {
            Cr02Form form = formService.getCr02FormById(id);
            return ResponseEntity.ok(form);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
