package com.aftercare.aftercare_portal.dto;

import com.aftercare.aftercare_portal.entity.Citizen;

import java.time.LocalDate;
import java.time.Period;

public record CitizenLookupDto(
        String nic,
        String fullName,
        String fullNameSinhala,
        String dateOfBirth,
        Integer dobYear,
        Integer dobMonth,
        Integer dobDay,
        Integer ageYears,
        Integer ageMonths,
        Integer ageDays,
        String gender,
        String nationality,
        String ethnicity,
        String address,
        String addressDistrict,
        String addressDivision,
        String addressGnDivision,
        String occupation,
        String maritalStatus,
        boolean alive) {

    public static CitizenLookupDto from(Citizen c) {
        LocalDate dob = c.getDateOfBirth();
        Integer dobYear = null, dobMonth = null, dobDay = null;
        Integer ageYears = null, ageMonths = null, ageDays = null;

        if (dob != null) {
            dobYear  = dob.getYear();
            dobMonth = dob.getMonthValue();
            dobDay   = dob.getDayOfMonth();
            Period age = Period.between(dob, LocalDate.now());
            ageYears  = age.getYears();
            ageMonths = age.getMonths();
            ageDays   = age.getDays();
        }

        return new CitizenLookupDto(
                c.getNic(),
                c.getFullName(),
                c.getFullNameSinhala(),
                dob != null ? dob.toString() : null,
                dobYear, dobMonth, dobDay,
                ageYears, ageMonths, ageDays,
                c.getGender() != null ? c.getGender().name().toLowerCase() : null,
                c.getNationality(),
                c.getEthnicity(),
                c.getAddress(),
                c.getAddressDistrict(),
                c.getAddressDivision(),
                c.getAddressGnDivision(),
                c.getOccupation(),
                c.getMaritalStatus(),
                c.isAlive()
        );
    }
}
