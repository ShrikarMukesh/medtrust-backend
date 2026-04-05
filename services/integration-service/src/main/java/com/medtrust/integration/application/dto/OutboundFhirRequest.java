package com.medtrust.integration.application.dto;

import jakarta.validation.constraints.NotBlank;

public record OutboundFhirRequest(
        @NotBlank String resourceType,
        @NotBlank String externalSystemId,
        @NotBlank String internalResourceId) {
}
