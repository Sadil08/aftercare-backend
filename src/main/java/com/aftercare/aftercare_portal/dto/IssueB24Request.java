package com.aftercare.aftercare_portal.dto;

import jakarta.validation.constraints.NotNull;

public record IssueB24Request(
        @NotNull(message = "Identity verification status is required") Boolean identityVerified,

        @NotNull(message = "Residence verification status is required") Boolean residenceVerified) {
}
