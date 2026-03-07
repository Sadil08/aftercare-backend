package com.aftercare.aftercare_portal.entity.document;

import com.aftercare.aftercare_portal.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.time.LocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "official_documents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class OfficialDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "issued_by_user_id")
    private User issuedBy;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    private String cryptographicHash;

    protected OfficialDocument(User issuedBy, String hash) {
        this.issuedBy = issuedBy;
        this.issuedAt = LocalDateTime.now();
        this.cryptographicHash = hash;
    }

    public abstract String getDocumentType();
}
