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
    private final LoginAttemptService loginAttemptService;

    public AuthService(UserRepository userRepository, SectorRepository sectorRepository,
            PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
            AuthenticationManager authenticationManager,
            LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.sectorRepository = sectorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.loginAttemptService = loginAttemptService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("A user with this username already exists.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("A user with this email already exists.");
        }

        User user = new User(
                request.username(),
                request.email(),
                request.fullName(),
                passwordEncoder.encode(request.password()),
                request.phone(),
                request.nicNo());

        // Assign role — default to FAMILY
        Role role = Role.FAMILY;
        if (request.role() != null && !request.role().isBlank()) {
            try {
                role = Role.valueOf(request.role().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + request.role());
            }
        }

        // Block self-registration for official roles (these are seeded by the system)
        if (role == Role.GRAMA_NILADHARI || role == Role.REGISTRAR || role == Role.DOCTOR) {
            throw new IllegalArgumentException(
                    "Official accounts (Grama Niladhari, Registrar, Doctor) cannot be registered through the platform.");
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
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), rolesStr, sectorCode);

        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getNicNo(),
                user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()),
                user.getDoctorId());
    }

    public AuthResponse login(LoginRequest request) {
        if (loginAttemptService.isBlocked(request.username())) {
            throw new SecurityException("Too many failed login attempts. Try again in 10 minutes.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (Exception e) {
            loginAttemptService.recordFailure(request.username());
            throw e;
        }

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (user.isLocked()) {
            throw new SecurityException("Account is locked. Contact administration.");
        }

        loginAttemptService.recordSuccess(request.username());

        String sectorCode = user.getSector() != null ? user.getSector().getCode() : null;
        String rolesStr = user.getRoles().stream().map(Enum::name).collect(Collectors.joining(","));
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), rolesStr, sectorCode);

        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getNicNo(),
                user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()),
                user.getDoctorId());
    }
}
