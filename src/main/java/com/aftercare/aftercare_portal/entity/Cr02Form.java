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
    private String typeOfDeath;

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
    private String deathLocation;

    // (4) Cause Established?
    private String causeEstablished;

    // (5) Cause of Death
    private String causeOfDeath;

    // (6) ICD Code
    private String icdCode;

    // (7) Burial/Cremation Place
    private String burialPlace;

    // Informant Details
    private String informantCapacity;
    private String informantId;
    private String informantName;
    private String informantAddress;
    private String informantPhone;
    private String informantEmail;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Tracker Fields
    private String familyNicNo;
    private String currentStage;

    private LocalDateTime submissionTimestamp;

    @PrePersist
    protected void onCreate() {
        if (currentStage == null) {
            currentStage = "SUBMITTED_BY_GN";
        }
        submissionTimestamp = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
