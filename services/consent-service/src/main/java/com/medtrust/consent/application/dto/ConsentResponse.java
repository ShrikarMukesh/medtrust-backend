package com.medtrust.consent.application.dto;

public record ConsentResponse(
        String id,
        String patientId,
        String grantedToUserId,
        String scope,
        String status,
        String validFrom,
        String validUntil,
        String reason,
        boolean currentlyValid,
        String revokedAt,
        String createdAt,
        String updatedAt) {
}
