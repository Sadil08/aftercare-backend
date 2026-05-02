package com.aftercare.aftercare_portal.entity;

import com.aftercare.aftercare_portal.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(name = "nic", unique = true, nullable = false)
    private String nicNo;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String passwordHash;

    private String phone;

    @Column(unique = true, nullable = false)
    private String email;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "sector_id")
    private Sector sector;

    @Column(nullable = false)
    private boolean enabled = false;

    @Column(nullable = false)
    private boolean locked = false;

    /**
     * Alphanumeric identifier unique to Doctor accounts (e.g. DOC-A1B2C3).
     * Null for all non-doctor roles. Used by families to route cases to a specific doctor.
     */
    @Column(name = "doctor_id", unique = true)
    private String doctorId;

    @Column(nullable = false)
    private boolean phoneVerified = false;

    private String pendingOtp;

    private LocalDateTime otpExpiresAt;

    public User(String username, String email, String fullName, String passwordHash, String phone, String nicNo) {
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.nicNo = nicNo;
    }

    public void grantRole(Role role) {
        this.roles.add(role);
        // Auto-generate a unique Doctor ID the first time the DOCTOR role is granted
        if (role == Role.DOCTOR && this.doctorId == null) {
            this.doctorId = generateDoctorId();
        }
    }

    /** Generates a unique alphanumeric Doctor ID, e.g. DOC-A1B2C3. */
    private static String generateDoctorId() {
        String raw = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return "DOC-" + raw.substring(0, 6);
    }

    public void assignSector(Sector sector) {
        this.sector = sector;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void lock() {
        this.locked = true;
    }

    public void unlock() {
        this.locked = false;
    }

    public String issueOtp() {
        this.pendingOtp = String.format("%06d", new Random().nextInt(1_000_000));
        this.otpExpiresAt = LocalDateTime.now().plusMinutes(5);
        return this.pendingOtp;
    }

    public boolean verifyOtp(String code) {
        if (this.pendingOtp == null || this.otpExpiresAt == null) return false;
        if (LocalDateTime.now().isAfter(this.otpExpiresAt)) return false;
        if (!this.pendingOtp.equals(code)) return false;
        this.pendingOtp = null;
        this.otpExpiresAt = null;
        this.phoneVerified = true;
        this.enabled = true;
        return true;
    }

    public void markPhoneVerified() {
        this.phoneVerified = true;
        this.enabled = true;
        this.pendingOtp = null;
        this.otpExpiresAt = null;
    }

    public void updatePassword(String encodedPassword) {
        this.passwordHash = encodedPassword;
    }

    public void updatePhone(String newPhone) {
        this.phone = newPhone;
    }

    public void updateEmail(String newEmail) {
        this.email = newEmail;
    }
}
