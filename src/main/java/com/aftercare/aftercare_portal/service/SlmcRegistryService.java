package com.aftercare.aftercare_portal.service;

import org.springframework.stereotype.Service;

/**
 * Validates SLMC (Sri Lanka Medical Council) registration numbers.
 * In production this would call the SLMC API or query a synced registry table.
 * Currently enforces format rules only: 5-digit numeric string.
 */
@Service
public class SlmcRegistryService {

    private static final java.util.regex.Pattern SLMC_FORMAT = java.util.regex.Pattern.compile("^\\d{5}$");

    public void validate(String slmcNo) {
        if (slmcNo == null || slmcNo.isBlank()) {
            throw new IllegalArgumentException("SLMC registration number is required to issue a B-12 certificate.");
        }
        if (!SLMC_FORMAT.matcher(slmcNo.trim()).matches()) {
            throw new IllegalArgumentException(
                    "Invalid SLMC registration number '" + slmcNo + "'. Must be a 5-digit numeric code.");
        }
    }
}
