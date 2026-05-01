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

    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String address;

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

    public void markDeceased() {
        this.alive = false;
    }
}
