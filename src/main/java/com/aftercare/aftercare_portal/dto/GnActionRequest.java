package com.aftercare.aftercare_portal.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * GN action request — action must be "APPROVE" or "REQUEST_MEDICAL".
 */
public record GnActionRequest(
        @NotBlank(message = "Action is required") String action) {
}
