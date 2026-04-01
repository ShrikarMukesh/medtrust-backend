package com.medtrust.patient.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RegisterPatientRequest(
        @NotBlank String mrn,
        @NotBlank String firstName,
        String middleName,
        @NotBlank String lastName,
        @NotBlank String dateOfBirth,
        @NotBlank String gender,
        String bloodType,
        @NotNull @Valid ContactInfoRequest contactInfo,
        @Valid EmergencyContactRequest emergencyContact,
        @Valid InsuranceInfoRequest insuranceInfo,
        List<String> allergies) {

    public record ContactInfoRequest(
            @NotBlank String phone,
            @NotBlank String email,
            @NotBlank String address,
            String city,
            String state,
            String zipCode) {
    }

    public record EmergencyContactRequest(
            @NotBlank String name,
            @NotBlank String relationship,
            @NotBlank String phone) {
    }

    public record InsuranceInfoRequest(
            @NotBlank String provider,
            @NotBlank String policyNumber,
            String groupNumber,
            String expirationDate) {
    }
}
