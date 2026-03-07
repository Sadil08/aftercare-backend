package com.aftercare.aftercare_portal.entity.document;

import com.aftercare.aftercare_portal.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Entity
@Table(name = "form_b2")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FormB2 extends OfficialDocument {

    @Column(unique = true, nullable = false)
    private String certificateSerialNumber;

    public FormB2(User issuedBy, String hash, String serialNumber) {
        super(issuedBy, hash);
        this.certificateSerialNumber = serialNumber;
    }

    @Override
    public String getDocumentType() {
        return "B-2_DEATH_CERTIFICATE";
    }
}
