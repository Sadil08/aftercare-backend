package com.aftercare.aftercare_portal.service;

import com.aftercare.aftercare_portal.dto.AuthResponse;
import com.aftercare.aftercare_portal.dto.LoginRequest;
import com.aftercare.aftercare_portal.dto.RegisterRequest;
import com.aftercare.aftercare_portal.entity.Sector;
import com.aftercare.aftercare_portal.entity.User;
import com.aftercare.aftercare_portal.enums.Role;
import com.aftercare.aftercare_portal.repository.SectorRepository;
import com.aftercare.aftercare_portal.repository.UserRepository;
import com.aftercare.aftercare_portal.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final SectorRepository sectorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, SectorRepository sectorRepository,
            PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.sectorRepository = sectorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByNic(request.nic())) {
            throw new IllegalArgumentException("A user with this NIC already exists.");
        }

        User user = new User(
                request.nic(),
                request.fullName(),
                passwordEncoder.encode(request.password()),
                request.phone());

        if (request.email() != null) {
            user.setEmail(request.email());
        }

        // Assign role — default to CITIZEN
        Role role = Role.CITIZEN;
        if (request.role() != null && !request.role().isBlank()) {
            try {
                role = Role.valueOf(request.role().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + request.role());
            }
        }
        user.grantRole(role);

        // Assign sector for GN/DOCTOR
        if (request.sectorCode() != null && !request.sectorCode().isBlank()) {
            Sector sector = sectorRepository.findByCode(request.sectorCode())
                    .orElseThrow(() -> new IllegalArgumentException("Sector not found: " + request.sectorCode()));
            user.assignSector(sector);
        }

        userRepository.save(user);

        String sectorCode = user.getSector() != null ? user.getSector().getCode() : null;
        String rolesStr = user.getRoles().stream().map(Enum::name).collect(Collectors.joining(","));
        String token = jwtUtil.generateToken(user.getId(), user.getNic(), rolesStr, sectorCode);

        return new AuthResponse(
                token,
                user.getId(),
                user.getNic(),
                user.getFullName(),
                user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.nic(), request.password()));

        User user = userRepository.findByNic(request.nic())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (user.isLocked()) {
            throw new SecurityException("Account is locked. Contact administration.");
        }

        String sectorCode = user.getSector() != null ? user.getSector().getCode() : null;
        String rolesStr = user.getRoles().stream().map(Enum::name).collect(Collectors.joining(","));
        String token = jwtUtil.generateToken(user.getId(), user.getNic(), rolesStr, sectorCode);

        return new AuthResponse(
                token,
                user.getId(),
                user.getNic(),
                user.getFullName(),
                user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()));
    }
}
