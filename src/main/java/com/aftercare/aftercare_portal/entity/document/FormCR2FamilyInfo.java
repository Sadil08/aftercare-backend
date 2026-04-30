package com.aftercare.aftercare_portal.entity.document;

import com.aftercare.aftercare_portal.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Entity
@Table(name = "form_cr2_family_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FormCR2FamilyInfo extends OfficialDocument {

    @Column(name = "family_report_json", columnDefinition = "TEXT")
    private String familyReportJson;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String cr2FormData;

    public FormCR2FamilyInfo(User issuedBy, String hash, String familyReportJson, String cr2FormData) {
        super(issuedBy, hash);
        this.familyReportJson = familyReportJson;
        this.cr2FormData = cr2FormData != null ? cr2FormData : familyReportJson;
    }

    public FormCR2FamilyInfo(User issuedBy, String hash, String cr2FormData) {
        this(issuedBy, hash, null, cr2FormData);
    }

    @Override
    public String getDocumentType() {
        return "CR-2_FAMILY_DECLARATION";
    }
}
