package com.aftercare.aftercare_portal.controller;

import com.aftercare.aftercare_portal.entity.User;
import com.aftercare.aftercare_portal.enums.Role;
import com.aftercare.aftercare_portal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/fix-db")
    public String fixDb() {
        try {
            jdbcTemplate.execute("ALTER TABLE user_roles DROP CONSTRAINT IF EXISTS user_roles_role_check");
            return "Fixed!";
        } catch (Exception e) {
            return "Error: " + e.getMessage() + " | Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "none");
        }
    }

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

    @GetMapping("/cemeteries")
    public ResponseEntity<List<Map<String, String>>> getCemeteries() {
        List<User> cemeteries = userRepository.findByRole(Role.CEMETERY);
        List<Map<String, String>> result = cemeteries.stream()
                .map(u -> Map.of(
                        "username", u.getUsername(),
                        "fullName", u.getFullName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}
