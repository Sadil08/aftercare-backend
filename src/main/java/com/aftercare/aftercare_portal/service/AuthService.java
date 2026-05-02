package com.aftercare.aftercare_portal.service;

import com.aftercare.aftercare_portal.dto.AuthResponse;
import com.aftercare.aftercare_portal.dto.LoginRequest;
import com.aftercare.aftercare_portal.dto.RegisterRequest;
import com.aftercare.aftercare_portal.entity.Sector;
import com.aftercare.aftercare_portal.entity.User;
import com.aftercare.aftercare_portal.enums.Role;
import com.aftercare.aftercare_portal.repository.CitizenRepository;
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
    private final CitizenRepository citizenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final LoginAttemptService loginAttemptService;

    public AuthService(UserRepository userRepository, SectorRepository sectorRepository,
            CitizenRepository citizenRepository,
            PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
            AuthenticationManager authenticationManager,
            LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.sectorRepository = sectorRepository;
        this.citizenRepository = citizenRepository;
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

        // Enforce one account per NIC
        if (request.nicNo() != null && !request.nicNo().isBlank()
                && userRepository.existsByNicNo(request.nicNo())) {
            throw new IllegalArgumentException("An account is already registered for this NIC.");
        }

        // Validate NIC against the national citizen registry (rejects fake NICs)
        if (request.nicNo() != null && !request.nicNo().isBlank()) {
            var citizen = citizenRepository.findByNic(request.nicNo())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "NIC not found in the national registry. Registration is not permitted."));
            if (!citizen.isAlive()) {
                throw new IllegalArgumentException(
                        "This NIC belongs to a deceased person. Registration is not permitted.");
            }

            // Full name must match the registry record (prevents registering under someone else's NIC)
            if (request.fullName() == null || !request.fullName().trim().equalsIgnoreCase(citizen.getFullName().trim())) {
                throw new IllegalArgumentException(
                        "The full name entered does not match the national registry for this NIC.");
            }

            // District must match the registry record
            if (request.district() == null || request.district().isBlank()) {
                throw new IllegalArgumentException("District is required for registration.");
            }
            if (citizen.getAddressDistrict() == null
                    || !request.district().trim().equalsIgnoreCase(citizen.getAddressDistrict().trim())) {
                throw new IllegalArgumentException(
                        "The district entered does not match the national registry for this NIC.");
            }
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

        // Issue OTP immediately so the user receives it on the same request
        String otp = user.issueOtp();
        userRepository.save(user);

        // In production: send SMS via Twilio/etc. to user.getPhone()
        System.out.println("=== Registration OTP for " + user.getUsername()
                + " (" + maskPhone(user.getPhone()) + "): " + otp + " (expires in 5 minutes) ===");

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
                user.getDoctorId(),
                user.isPhoneVerified());
    }

    @Transactional
    public String sendOtp(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        String otp = user.issueOtp();
        userRepository.save(user);

        // In production: send SMS via Twilio/etc. to user.getPhone()
        // For development: log to console so testers can see the code
        System.out.println("=== OTP for " + username + " (" + user.getPhone() + "): " + otp + " (expires in 5 minutes) ===");
        return "OTP sent to phone number ending in " + maskPhone(user.getPhone());
    }

    /** DEV ONLY — returns the pending OTP for a user so the frontend can display it as a mock SMS. */
    public String getDevOtp(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (user.getPendingOtp() == null) {
            return null;
        }
        return user.getPendingOtp();
    }

    @Transactional
    public void verifyPhone(String username, String otpCode) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (!user.verifyOtp(otpCode)) {
            throw new SecurityException("Invalid or expired OTP. Request a new code.");
        }
        userRepository.save(user);
        // Clear any login-attempt block accumulated while the account was unverified
        loginAttemptService.recordSuccess(username);
    }

    @Transactional
    public String initiatePasswordReset(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("No account found for that username."));
        String otp = user.issueOtp();
        userRepository.save(user);
        System.out.println("=== Password-reset OTP for " + username
                + " (" + maskPhone(user.getPhone()) + "): " + otp + " (expires in 5 minutes) ===");
        return "Password-reset OTP sent to phone number ending in " + maskPhone(user.getPhone());
    }

    @Transactional
    public void resetPassword(String username, String otpCode, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("No account found for that username."));
        if (!user.verifyOtp(otpCode)) {
            throw new SecurityException("Invalid or expired OTP. Request a new code.");
        }
        user.updatePassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        loginAttemptService.recordSuccess(username);
    }

    @Transactional
    public void changePhone(String username, String oldPhone, String newPhone, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new SecurityException("Incorrect password.");
        }
        if (!oldPhone.trim().equals(user.getPhone() == null ? "" : user.getPhone().trim())) {
            throw new IllegalArgumentException("Old phone number does not match.");
        }
        user.updatePhone(newPhone.trim());
        userRepository.save(user);
    }

    @Transactional
    public void changeEmail(String username, String oldEmail, String newEmail, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new SecurityException("Incorrect password.");
        }
        if (!oldEmail.trim().equalsIgnoreCase(user.getEmail() == null ? "" : user.getEmail().trim())) {
            throw new IllegalArgumentException("Old email does not match.");
        }
        if (userRepository.existsByEmail(newEmail.trim())) {
            throw new IllegalArgumentException("That email address is already in use.");
        }
        user.updateEmail(newEmail.trim());
        userRepository.save(user);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "****";
        return "****" + phone.substring(phone.length() - 4);
    }

    public AuthResponse login(LoginRequest request) {
        if (loginAttemptService.isBlocked(request.username())) {
            throw new SecurityException("Too many failed login attempts. Try again in 10 minutes.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (org.springframework.security.authentication.DisabledException e) {
            // Don't penalise as a failed attempt — this is an unverified account, not wrong credentials
            throw e;
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
                user.getDoctorId(),
                user.isPhoneVerified());
    }
}
