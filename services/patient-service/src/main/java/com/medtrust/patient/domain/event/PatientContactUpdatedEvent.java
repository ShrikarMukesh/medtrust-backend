package com.medtrust.patient.domain.event;

import java.time.Instant;
import java.util.Map;

public record PatientContactUpdatedEvent(
        String patientId) implements DomainEvent {

    @Override
    public String eventType() {
        return "patient.contact_updated";
    }

    @Override
    public Instant occurredAt() {
        return Instant.now();
    }

    @Override
    public Map<String, Object> payload() {
        return Map.of("patientId", patientId);
    }
}
