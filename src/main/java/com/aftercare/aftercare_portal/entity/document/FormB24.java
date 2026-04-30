package com.aftercare.aftercare_portal.entity.document;

import com.aftercare.aftercare_portal.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.time.LocalDate;

@Entity
@Table(name = "form_b24")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FormB24 extends OfficialDocument {

    private String gramaDivision;

    private String registrarDivision;

    private String serialNo;

    private LocalDate deathDate;

    private String placeOfDeath;

    private String fullName;

    private String sex;

    private String race;

    private String age;

    private String profession;

    @Column(columnDefinition = "TEXT")
    private String causeOfDeath;

    private String informantName;

    @Column(columnDefinition = "TEXT")
    private String informantAddress;

    private String registrarName;

    private String signedAt;

    private LocalDate signDate;

    private String gnSignature;

    @Column(nullable = false)
    private boolean confirmed;

    public FormB24(User issuedBy, String hash, String gramaDivision, String registrarDivision, String serialNo,
            LocalDate deathDate, String placeOfDeath, String fullName, String sex, String race, String age,
            String profession, String causeOfDeath, String informantName, String informantAddress,
            String registrarName, String signedAt, LocalDate signDate, String gnSignature, boolean confirmed) {
        super(issuedBy, hash);
        this.gramaDivision = gramaDivision;
        this.registrarDivision = registrarDivision;
        this.serialNo = serialNo;
        this.deathDate = deathDate;
        this.placeOfDeath = placeOfDeath;
        this.fullName = fullName;
        this.sex = sex;
        this.race = race;
        this.age = age;
        this.profession = profession;
        this.causeOfDeath = causeOfDeath;
        this.informantName = informantName;
        this.informantAddress = informantAddress;
        this.registrarName = registrarName;
        this.signedAt = signedAt;
        this.signDate = signDate;
        this.gnSignature = gnSignature;
        this.confirmed = confirmed;
    }

    @Override
    public String getDocumentType() {
        return "B-24_HOME_REPORT";
    }
}
