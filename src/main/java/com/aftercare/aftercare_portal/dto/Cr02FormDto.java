package com.aftercare.aftercare_portal.dto;

import lombok.Data;

@Data
public class Cr02FormDto {
    private String typeOfDeath;
    
    // 2
    private Integer deathYear;
    private Integer deathMonth;
    private Integer deathDay;

    // 3
    private String district;
    private String dsDivision;
    private String regDivision;
    private String placeInSinhalaOrTamil;
    private String placeInEnglish;
    private String timeOfDeath;
    private String deathLocation;

    // 4
    private String causeEstablished;

    // 5
    private String causeOfDeath;

    // 6
    private String icdCode;

    // 7
    private String burialPlace;

    // Informant Details
    private String informantCapacity;
    private String informantId;
    private String informantName;
    private String informantAddress;
    private String informantPhone;
    private String informantEmail;
    
    // Tracker Fields
    private Long cr02FamilyUserId;
}
