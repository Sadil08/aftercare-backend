package com.aftercare.aftercare_portal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "b24_forms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class B24Form {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Header Details
    private String gramaDivision;
    private String registrarDivision;
    private String serialNo;

    // 1. Date and Place of Death
    private Integer deathYear;
    private Integer deathMonth;
    private Integer deathDay;
    private String placeOfDeath;

    // 2. Full Name
    private String fullName;

    // 3. Sex and Race
    private String sex;
    private String race;

    // 4. Age
    private String age;

    // 5. Rank or Profession
    private String profession;

    // 6. Cause of Death
    private String causeOfDeath;

    // 7. Informant Details
    private String informantName;
    private String informantAddress;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Tracker Fields
    private String familyNicNo;
    private String assignedRegistrarUsername;
    private String submittedByUsername;
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
