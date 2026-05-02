package com.aftercare.aftercare_portal.entity;

import com.aftercare.aftercare_portal.enums.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.time.LocalDate;

@Entity
@Table(name = "citizens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Citizen {

    @Id
    @Column(nullable = false)
    private String nic;

    @Column(nullable = false)
    private String fullName;

    private String fullNameSinhala;

    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String nationality;

    private String ethnicity;

    private String address;

    private String addressDistrict;

    private String addressDivision;

    private String addressGnDivision;

    private String occupation;

    private String maritalStatus;

    @Column(nullable = false)
    private boolean alive = true;

    public Citizen(String nic, String fullName, LocalDate dateOfBirth, Gender gender, String address) {
        this.nic = nic;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.address = address;
        this.alive = true;
    }

    public Citizen(String nic, String fullName, String fullNameSinhala,
                   LocalDate dateOfBirth, Gender gender,
                   String nationality, String ethnicity,
                   String address, String addressDistrict,
                   String addressDivision, String addressGnDivision,
                   String occupation, String maritalStatus) {
        this.nic = nic;
        this.fullName = fullName;
        this.fullNameSinhala = fullNameSinhala;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.nationality = nationality;
        this.ethnicity = ethnicity;
        this.address = address;
        this.addressDistrict = addressDistrict;
        this.addressDivision = addressDivision;
        this.addressGnDivision = addressGnDivision;
        this.occupation = occupation;
        this.maritalStatus = maritalStatus;
        this.alive = true;
    }

    public void markDeceased() {
        this.alive = false;
    }
}
