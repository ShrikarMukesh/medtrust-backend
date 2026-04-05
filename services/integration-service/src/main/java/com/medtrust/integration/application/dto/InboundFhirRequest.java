package com.medtrust.integration.application.dto;

import jakarta.validation.constraints.NotBlank;

public record InboundFhirRequest(
        @NotBlank String resourceType,
        @NotBlank String externalSystemId,
        @NotBlank String fhirPayload) {
}
