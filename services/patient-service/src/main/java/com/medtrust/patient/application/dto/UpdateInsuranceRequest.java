package com.medtrust.patient.application.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateInsuranceRequest(
        @NotBlank String provider,
        @NotBlank String policyNumber,
        String groupNumber,
        String expirationDate) {
}
