package com.aftercare.aftercare_portal.entity.document;

import com.aftercare.aftercare_portal.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.time.LocalDateTime;

@Entity
@Table(name = "form_b12")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FormB12 extends OfficialDocument {

    @Column(nullable = false)
    private boolean naturalDeath;

    @Column(nullable = false)
    private String icd10Code;

    @Column(name = "primary_cause", nullable = false)
    private String immediateCause;

    @Column(columnDefinition = "TEXT")
    private String antecedentCausesJson;

    @Column(columnDefinition = "TEXT")
    private String contributoryCausesJson;

    private LocalDateTime doctorViewedBodyAt;

    private String doctorDesignation;

    private String slmcRegistrationNo;

    public FormB12(User issuedBy, String hash, boolean naturalDeath, String icd10Code, String immediateCause,
            String antecedentCausesJson, String contributoryCausesJson, LocalDateTime doctorViewedBodyAt,
            String doctorDesignation, String slmcRegistrationNo) {
        super(issuedBy, hash);
        this.naturalDeath = naturalDeath;
        this.icd10Code = icd10Code;
        this.immediateCause = immediateCause;
        this.antecedentCausesJson = antecedentCausesJson;
        this.contributoryCausesJson = contributoryCausesJson;
        this.doctorViewedBodyAt = doctorViewedBodyAt;
        this.doctorDesignation = doctorDesignation;
        this.slmcRegistrationNo = slmcRegistrationNo;
    }

    public String getPrimaryCause() {
        return immediateCause;
    }

    @Override
    public String getDocumentType() {
        return "B-12_MEDICAL_CAUSE";
    }
}
