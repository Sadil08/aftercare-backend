package com.aftercare.aftercare_portal.entity.document;

import com.aftercare.aftercare_portal.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Entity
@Table(name = "form_b11")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FormB11 extends OfficialDocument {

    @Column(nullable = false)
    private String applicantRelationship;

    @Column(nullable = false)
    private boolean declarationTrue;

    public FormB11(User issuedBy, String hash, String relationship, boolean declarationTrue) {
        super(issuedBy, hash);
        this.applicantRelationship = relationship;
        this.declarationTrue = declarationTrue;
    }

    @Override
    public String getDocumentType() {
        return "B-11_FAMILY_APPLICATION";
    }
}
