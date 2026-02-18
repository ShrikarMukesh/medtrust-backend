package com.medtrust.clinical.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterPatientRequest(
        @NotBlank String mrn,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String dateOfBirth,
        @NotBlank String gender,
        @NotNull @Valid ContactInfoRequest contactInfo) {
    public record ContactInfoRequest(
            @NotBlank String phone,
            @NotBlank String email,
            @NotBlank String address) {
    }
}
