package com.aftercare.aftercare_portal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "cr02_forms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cr02Form {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // (1) Type of Death
    private String typeOfDeath; // e.g. "normal", "sudden"

    // (2) Date of Death
    private Integer deathYear;
    private Integer deathMonth;
    private Integer deathDay;

    // (3) Place of Death
    private String district;
    private String dsDivision;
    private String regDivision;
    private String placeInSinhalaOrTamil;
    private String placeInEnglish;
    private String timeOfDeath;
    private String deathLocation; // e.g. "hospital", "outside"

    // (4) Cause Established?
    private String causeEstablished; // "yes", "no"

    // (5) Cause of Death
    private String causeOfDeath;

    // (6) ICD Code
    private String icdCode;

    // (7) Burial/Cremation Place
    private String burialPlace;

    // Informant Details
    // (24) Capacity
    private String informantCapacity; // e.g., "husband_wife", "father_mother"
    // (25) Identification
    private String informantId;
    // (26) Name
    private String informantName;
    // (27) Address
    private String informantAddress;
    // (28) Contact
    private String informantPhone;
    private String informantEmail;

    @CreationTimestamp
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

    // Tracker Fields
    private Long familyUserId;
    private String currentStage; // e.g., SUBMITTED_BY_GN, REVIEW_BY_REGISTRAR, APPROVED, READY_FOR_PICKUP

    @PrePersist
    protected void onCreate() {
        if (currentStage == null) {
            currentStage = "SUBMITTED_BY_GN";
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
