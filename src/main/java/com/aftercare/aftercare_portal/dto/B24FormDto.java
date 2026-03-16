package com.aftercare.aftercare_portal.dto;

import lombok.Data;

@Data
public class B24FormDto {
    // Header
    private String b24GramaDivision;
    private String b24RegistrarDivision;
    private String b24SerialNo;

    // 1
    private Integer b24DeathYear;
    private Integer b24DeathMonth;
    private Integer b24DeathDay;
    private String b24PlaceOfDeath;

    // 2
    private String b24FullName;

    // 3
    private String b24Sex;
    private String b24Race;

    // 4
    private String b24Age;

    // 5
    private String b24Profession;

    // 6
    private String b24CauseOfDeath;

    // 7
    private String b24InformantName;
    private String b24InformantAddress;
    
    private Long b24FamilyUserId;
}
