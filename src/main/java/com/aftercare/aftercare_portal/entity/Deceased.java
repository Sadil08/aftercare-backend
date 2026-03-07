package com.aftercare.aftercare_portal.entity;

import com.aftercare.aftercare_portal.enums.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.time.LocalDate;

@Entity
@Table(name = "deceased")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Deceased {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    private String nic;

    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private LocalDate dateOfDeath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private String address;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sector_id")
    private Sector residenceSector;

    public Deceased(String fullName, String nic, LocalDate dateOfBirth, LocalDate dateOfDeath,
            Gender gender, String address, Sector residenceSector) {
        this.fullName = fullName;
        this.nic = nic;
        this.dateOfBirth = dateOfBirth;
        this.dateOfDeath = dateOfDeath;
        this.gender = gender;
        this.address = address;
        this.residenceSector = residenceSector;
    }
}
