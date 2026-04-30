package com.aftercare.aftercare_portal.controller;

import com.aftercare.aftercare_portal.entity.User;
import com.aftercare.aftercare_portal.enums.Role;
import com.aftercare.aftercare_portal.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/registrars")
    public ResponseEntity<List<Map<String, String>>> getRegistrars() {
        List<User> registrars = userRepository.findByRole(Role.REGISTRAR);
        List<Map<String, String>> result = registrars.stream()
                .map(u -> Map.of(
                        "username", u.getUsername(),
                        "fullName", u.getFullName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // ──── Admin: lock an account (death / retirement / misconduct) ────
    @PostMapping("/{userId}/lock")
    public ResponseEntity<Map<String, String>> lockUser(
            @PathVariable Long userId, Authentication auth) {
        requireAdmin(auth);
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        target.lock();
        userRepository.save(target);
        return ResponseEntity.ok(Map.of(
                "message", "Account locked.",
                "username", target.getUsername()));
    }

    // ──── Admin: unlock an account (successor takes over, retirement reversed) ────
    @PostMapping("/{userId}/unlock")
    public ResponseEntity<Map<String, String>> unlockUser(
            @PathVariable Long userId, Authentication auth) {
        requireAdmin(auth);
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        target.unlock();
        userRepository.save(target);
        return ResponseEntity.ok(Map.of(
                "message", "Account unlocked.",
                "username", target.getUsername()));
    }

    // ──── Admin: list all official accounts (GN, Doctor, Registrar) ────
    @GetMapping("/officials")
    public ResponseEntity<List<Map<String, Object>>> getOfficials(Authentication auth) {
        requireAdmin(auth);
        List<Role> officialRoles = List.of(Role.GRAMA_NILADHARI, Role.DOCTOR, Role.REGISTRAR);
        List<Map<String, Object>> result = officialRoles.stream()
                .flatMap(role -> userRepository.findByRole(role).stream())
                .distinct()
                .map(u -> Map.<String, Object>of(
                        "id", u.getId(),
                        "username", u.getUsername(),
                        "fullName", u.getFullName(),
                        "roles", u.getRoles().stream().map(Enum::name).collect(Collectors.toSet()),
                        "sectorCode", u.getSector() != null ? u.getSector().getCode() : "",
                        "locked", u.isLocked(),
                        "enabled", u.isEnabled()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    private void requireAdmin(Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new SecurityException("Only ADMIN users can perform this action.");
        }
    }
}
