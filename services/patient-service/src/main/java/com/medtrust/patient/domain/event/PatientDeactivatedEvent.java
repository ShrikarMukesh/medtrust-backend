package com.medtrust.patient.domain.event;

import java.time.Instant;
import java.util.Map;

public record PatientDeactivatedEvent(
        String patientId,
        String mrn) implements DomainEvent {

    @Override
    public String eventType() {
        return "patient.deactivated";
    }

    @Override
    public Instant occurredAt() {
        return Instant.now();
    }

    @Override
    public Map<String, Object> payload() {
        return Map.of(
                "patientId", patientId,
                "mrn", mrn);
    }
}
