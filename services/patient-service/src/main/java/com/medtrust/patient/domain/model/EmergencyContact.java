package com.medtrust.patient.domain.model;

public record EmergencyContact(
        String name,
        String relationship,
        String phone) {
}
