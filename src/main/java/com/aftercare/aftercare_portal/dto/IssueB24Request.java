package com.aftercare.aftercare_portal.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record IssueB24Request(
        @NotBlank(message = "GN division is required") String b24GramaDivision,
        @NotBlank(message = "Registrar division is required") String b24RegistrarDivision,
        String b24SerialNo,
        @NotNull(message = "Date of death is required") LocalDate deathDate,
        @NotBlank(message = "Place of death is required") String b24PlaceOfDeath,
        @NotBlank(message = "Deceased full name is required") String b24FullName,
        @NotBlank(message = "Sex is required") String b24Sex,
        String b24Race,
        @NotBlank(message = "Age is required") String b24Age,
        String b24Profession,
        @NotBlank(message = "Cause of death is required") String b24CauseOfDeath,
        @NotBlank(message = "Informant name is required") String b24InformantName,
        @NotBlank(message = "Informant address is required") String b24InformantAddress,
        String b24RegistrarName,
        String b24SignedAt,
        @NotNull(message = "Sign date is required") LocalDate b24SignDate,
        @NotBlank(message = "GN signature is required") String b24GNSignature,
        boolean b24Confirmed) {

    @AssertTrue(message = "B-24 confirmation is required")
    public boolean isB24Confirmed() {
        return b24Confirmed;
    }
}
