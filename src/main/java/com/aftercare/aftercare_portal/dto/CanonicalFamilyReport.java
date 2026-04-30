package com.aftercare.aftercare_portal.dto;

import com.aftercare.aftercare_portal.enums.Gender;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CanonicalFamilyReport(
        @NotNull WorkflowScenario workflowScenario,
        @NotBlank String sectorCode,
        String doctorId,
        boolean declarationConfirmed,
        @NotNull @Valid DeceasedInfo deceased,
        @NotNull @Valid DeathInfo death,
        @Valid MaternalInfo maternal,
        @NotNull @Valid InformantInfo informant) {

    @AssertTrue(message = "Workflow scenario must be NATURAL_DEATH_HOME")
    public boolean hasSupportedWorkflowScenario() {
        return workflowScenario == WorkflowScenario.NATURAL_DEATH_HOME;
    }

    @AssertTrue(message = "Declaration must be confirmed")
    public boolean isDeclarationConfirmed() {
        return declarationConfirmed;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DeceasedInfo(
            @NotNull IdentificationStatus identificationStatus,
            String nic,
            String passportCountry,
            String passportNumber,
            String fullNameOfficialLanguage,
            @NotBlank String fullNameEnglish,
            LocalDate dateOfBirth,
            Integer ageYears,
            Integer ageMonths,
            Integer ageDays,
            @NotBlank String nationality,
            @NotNull Gender gender,
            String race,
            @NotNull @Valid AddressInfo permanentAddress,
            String profession,
            Boolean pensionStatus,
            String fatherNic,
            String fatherName,
            String motherNic,
            String motherName) {

        @AssertTrue(message = "Provide dateOfBirth or ageYears")
        public boolean hasDateOfBirthOrAgeYears() {
            return dateOfBirth != null || ageYears != null;
        }

        @AssertTrue(message = "Sri Lankan identification requires NIC")
        public boolean sriLankanIdentificationIsComplete() {
            return identificationStatus != IdentificationStatus.IDENTIFIED_SRI_LANKAN
                    || hasText(nic);
        }

        @AssertTrue(message = "Foreigner identification requires passport country and number")
        public boolean foreignIdentificationIsComplete() {
            return identificationStatus != IdentificationStatus.IDENTIFIED_FOREIGNER
                    || (hasText(passportCountry) && hasText(passportNumber));
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AddressInfo(
            @NotBlank String fullText,
            @NotBlank String district,
            @NotBlank String dsDivision,
            @NotBlank String gnDivision) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DeathInfo(
            @NotNull LocalDate date,
            String time,
            @AssertFalse(message = "Natural death at home workflow cannot be marked as hospital death") boolean occurredInHospital,
            String placeOfficialLanguage,
            @NotBlank String placeEnglish,
            @NotBlank String placeDistrict,
            @NotBlank String placeDsDivision,
            @NotBlank String registrationDivision,
            Boolean causeKnownByFamily,
            String familyNarrative,
            String burialOrCremationPlace) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MaternalInfo(
            Boolean wasPregnantAtDeath,
            Boolean gaveBirthWithin42Days,
            Boolean hadAbortion,
            Integer daysSinceBirthOrAbortion) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InformantInfo(
            @NotNull InformantCapacity capacity,
            String otherCapacityText,
            @NotBlank String nicOrPassport,
            @NotBlank String fullName,
            @NotBlank String postalAddress,
            @NotBlank String mobile,
            String landline,
            String email) {

        @AssertTrue(message = "Other informant capacity requires a description")
        public boolean hasOtherCapacityTextWhenRequired() {
            return capacity != InformantCapacity.OTHER || hasText(otherCapacityText);
        }
    }

    public enum WorkflowScenario {
        NATURAL_DEATH_HOME
    }

    public enum IdentificationStatus {
        IDENTIFIED_SRI_LANKAN,
        IDENTIFIED_FOREIGNER,
        NOT_IDENTIFIED
    }

    public enum InformantCapacity {
        HUSBAND_WIFE,
        FATHER_MOTHER,
        SON_DAUGHTER,
        BROTHER_SISTER,
        RELATIVE,
        OTHER
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
