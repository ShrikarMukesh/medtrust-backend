package com.medtrust.patient.domain.model;

public record ContactInfo(
        String phone,
        String email,
        String address,
        String city,
        String state,
        String zipCode) {
}
