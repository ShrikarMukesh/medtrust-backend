package com.medtrust.consent.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GrantConsentRequest(
        @NotBlank String patientId,
        @NotBlank String grantedToUserId,
        @NotNull String scope,
        String validFrom,
        String validUntil,
        String reason) {
}
