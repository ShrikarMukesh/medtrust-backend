package com.medtrust.consent.application.dto;

public record ConsentVerificationResponse(
        boolean hasConsent,
        String patientId,
        String userId,
        String scope,
        String consentId,
        String validUntil) {
}
