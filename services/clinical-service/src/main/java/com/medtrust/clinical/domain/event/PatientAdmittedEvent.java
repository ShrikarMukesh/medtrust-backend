package com.medtrust.clinical.domain.event;

import java.time.Instant;
import java.util.Map;

public record PatientAdmittedEvent(
        String encounterId,
        String patientId) implements DomainEvent {

    @Override
    public String eventType() {
        return "patient.admitted";
    }

    @Override
    public Instant occurredAt() {
        return Instant.now();
    }

    @Override
    public Map<String, Object> payload() {
        return Map.of(
                "encounterId", encounterId,
                "patientId", patientId);
    }
}
