package com.medtrust.clinical.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateEncounterRequest(
        @NotBlank String patientId) {
}
