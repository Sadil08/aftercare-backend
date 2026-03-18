package com.aftercare.aftercare_portal.entity.document;

import com.aftercare.aftercare_portal.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Entity
@Table(name = "form_b12")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FormB12 extends OfficialDocument {

    @Column(nullable = false)
    private boolean naturalDeath;

    @Column(nullable = false)
    private String icd10Code;

    @Column(nullable = false)
    private String primaryCause;

    public FormB12(User issuedBy, String hash, boolean naturalDeath, String icd10Code, String primaryCause) {
        super(issuedBy, hash);
        this.naturalDeath = naturalDeath;
        this.icd10Code = icd10Code;
        this.primaryCause = primaryCause;
    }

    @Override
    public String getDocumentType() {
        return "B-12_MEDICAL_CAUSE";
    }
}
