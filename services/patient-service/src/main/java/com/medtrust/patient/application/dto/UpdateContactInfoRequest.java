package com.medtrust.patient.application.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateContactInfoRequest(
        @NotBlank String phone,
        @NotBlank String email,
        @NotBlank String address,
        String city,
        String state,
        String zipCode) {
}
