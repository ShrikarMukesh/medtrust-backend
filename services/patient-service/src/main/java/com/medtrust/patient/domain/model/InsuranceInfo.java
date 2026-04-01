package com.medtrust.patient.domain.model;

public record InsuranceInfo(
        String provider,
        String policyNumber,
        String groupNumber,
        String expirationDate) {
}
