package com.medtrust.patient.application.dto;

import java.util.List;

public record PatientResponse(
        String id,
        String mrn,
        String firstName,
        String middleName,
        String lastName,
        String fullName,
        String dateOfBirth,
        String gender,
        String bloodType,
        ContactInfoResponse contactInfo,
        EmergencyContactResponse emergencyContact,
        InsuranceInfoResponse insuranceInfo,
        List<String> allergies,
        boolean active,
        String createdAt,
        String updatedAt) {

    public record ContactInfoResponse(
            String phone,
            String email,
            String address,
            String city,
            String state,
            String zipCode) {
    }

    public record EmergencyContactResponse(
            String name,
            String relationship,
            String phone) {
    }

    public record InsuranceInfoResponse(
            String provider,
            String policyNumber,
            String groupNumber,
            String expirationDate) {
    }
}
