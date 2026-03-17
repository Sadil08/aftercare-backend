package com.aftercare.aftercare_portal.controller;

import com.aftercare.aftercare_portal.entity.User;
import com.aftercare.aftercare_portal.enums.Role;
import com.aftercare.aftercare_portal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
