package com.aftercare.aftercare_portal.entity;

import com.aftercare.aftercare_portal.dto.CanonicalFamilyReport;
import com.aftercare.aftercare_portal.enums.Gender;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    private String fullNameOfficialLanguage;

    private String fullNameEnglish;

    private String nic;

    @Enumerated(EnumType.STRING)
    private CanonicalFamilyReport.IdentificationStatus identificationStatus;

    private String passportCountry;

    private String passportNumber;

    private LocalDate dateOfBirth;

    private Integer ageYears;

    private Integer ageMonths;

    private Integer ageDays;

    @Column(nullable = false)
    private LocalDate dateOfDeath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private String address;

    private String nationality;

    private String race;

    private String profession;

    private Boolean pensionStatus;

    private String fatherNic;

    private String fatherName;

    private String motherNic;

    private String motherName;

    private String permanentAddressFullText;

    private String permanentDistrict;

    private String permanentDsDivision;

    private String permanentGnDivision;

    private Boolean maternalWasPregnantAtDeath;

    private Boolean maternalGaveBirthWithin42Days;

    private Boolean maternalHadAbortion;

    private Integer maternalDaysSinceBirthOrAbortion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sector_id")
    private Sector residenceSector;

    public Deceased(String fullName, String nic, LocalDate dateOfBirth, LocalDate dateOfDeath,
            Gender gender, String address, Sector residenceSector) {
        this(
                fullName,
                nic,
                CanonicalFamilyReport.IdentificationStatus.IDENTIFIED_SRI_LANKAN,
                null,
                null,
                null,
                dateOfBirth,
                null,
                null,
                null,
                null,
                dateOfDeath,
                gender,
                address,
                null,
                null,
                null,
                null,
                null,
                null,
                address,
                residenceSector != null ? residenceSector.getDistrict() : null,
                null,
                residenceSector != null ? residenceSector.getName() : null,
                null,
                null,
                null,
                null,
                residenceSector);
    }

    public Deceased(
            String fullNameEnglish,
            String nic,
            CanonicalFamilyReport.IdentificationStatus identificationStatus,
            String passportCountry,
            String passportNumber,
            String fullNameOfficialLanguage,
            LocalDate dateOfBirth,
            Integer ageYears,
            Integer ageMonths,
            Integer ageDays,
            String nationality,
            LocalDate dateOfDeath,
            Gender gender,
            String permanentAddressFullText,
            String permanentDistrict,
            String permanentDsDivision,
            String permanentGnDivision,
            String race,
            String profession,
            Boolean pensionStatus,
            String fatherNic,
            String fatherName,
            String motherNic,
            String motherName,
            Boolean maternalWasPregnantAtDeath,
            Boolean maternalGaveBirthWithin42Days,
            Boolean maternalHadAbortion,
            Integer maternalDaysSinceBirthOrAbortion,
            Sector residenceSector) {
        this.fullName = fullNameEnglish;
        this.fullNameEnglish = fullNameEnglish;
        this.nic = nic;
        this.identificationStatus = identificationStatus;
        this.passportCountry = passportCountry;
        this.passportNumber = passportNumber;
        this.fullNameOfficialLanguage = fullNameOfficialLanguage;
        this.dateOfBirth = dateOfBirth;
        this.ageYears = ageYears;
        this.ageMonths = ageMonths;
        this.ageDays = ageDays;
        this.nationality = nationality;
        this.dateOfDeath = dateOfDeath;
        this.gender = gender;
        this.address = permanentAddressFullText;
        this.permanentAddressFullText = permanentAddressFullText;
        this.permanentDistrict = permanentDistrict;
        this.permanentDsDivision = permanentDsDivision;
        this.permanentGnDivision = permanentGnDivision;
        this.race = race;
        this.profession = profession;
        this.pensionStatus = pensionStatus;
        this.fatherNic = fatherNic;
        this.fatherName = fatherName;
        this.motherNic = motherNic;
        this.motherName = motherName;
        this.maternalWasPregnantAtDeath = maternalWasPregnantAtDeath;
        this.maternalGaveBirthWithin42Days = maternalGaveBirthWithin42Days;
        this.maternalHadAbortion = maternalHadAbortion;
        this.maternalDaysSinceBirthOrAbortion = maternalDaysSinceBirthOrAbortion;
        this.residenceSector = residenceSector;
    }

    public String getDisplayFullName() {
        return fullNameEnglish != null ? fullNameEnglish : fullName;
    }

    public String getDisplayAddress() {
        return permanentAddressFullText != null ? permanentAddressFullText : address;
    }
}
