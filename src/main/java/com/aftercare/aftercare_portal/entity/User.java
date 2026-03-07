package com.aftercare.aftercare_portal.entity;

import com.aftercare.aftercare_portal.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nic;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String passwordHash;

    private String phone;
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
    private boolean enabled = true;

    @Column(nullable = false)
    private boolean locked = false;

    public User(String nic, String fullName, String passwordHash, String phone) {
        this.nic = nic;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
        this.phone = phone;
    }

    public void grantRole(Role role) {
        this.roles.add(role);
    }

    public void assignSector(Sector sector) {
        this.sector = sector;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void lock() {
        this.locked = true;
    }

    public void unlock() {
        this.locked = false;
    }
}
