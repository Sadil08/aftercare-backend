package com.aftercare.aftercare_portal.controller;

import com.aftercare.aftercare_portal.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final AuthService authService;

    public ProfileController(AuthService authService) {
        this.authService = authService;
    }

    @PatchMapping("/phone")
    public ResponseEntity<?> changePhone(Authentication auth, @RequestBody Map<String, String> body) {
        String oldPhone = body.get("oldPhone");
        String newPhone = body.get("newPhone");
        String password = body.get("password");
        if (oldPhone == null || newPhone == null || password == null
                || oldPhone.isBlank() || newPhone.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "oldPhone, newPhone, and password are required"));
        }
        try {
            authService.changePhone(auth.getName(), oldPhone, newPhone, password);
            return ResponseEntity.ok(Map.of("message", "Phone number updated successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        }
    }

    @PatchMapping("/email")
    public ResponseEntity<?> changeEmail(Authentication auth, @RequestBody Map<String, String> body) {
        String oldEmail = body.get("oldEmail");
        String newEmail = body.get("newEmail");
        String password = body.get("password");
        if (oldEmail == null || newEmail == null || password == null
                || oldEmail.isBlank() || newEmail.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "oldEmail, newEmail, and password are required"));
        }
        try {
            authService.changeEmail(auth.getName(), oldEmail, newEmail, password);
            return ResponseEntity.ok(Map.of("message", "Email address updated successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        }
    }
}
