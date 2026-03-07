package com.aftercare.aftercare_portal.entity.document;

import com.aftercare.aftercare_portal.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Entity
@Table(name = "form_b24")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FormB24 extends OfficialDocument {

    @Column(nullable = false)
    private boolean identityVerified;

    @Column(nullable = false)
    private boolean residenceVerified;

    public FormB24(User issuedBy, String hash, boolean identityVerified, boolean residenceVerified) {
        super(issuedBy, hash);
        this.identityVerified = identityVerified;
        this.residenceVerified = residenceVerified;
    }

    @Override
    public String getDocumentType() {
        return "B-24_HOME_REPORT";
    }
}
