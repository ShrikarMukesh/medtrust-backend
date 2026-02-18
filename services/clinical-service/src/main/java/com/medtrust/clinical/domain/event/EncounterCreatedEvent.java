package com.medtrust.clinical.domain.event;

import java.time.Instant;
import java.util.Map;

public record EncounterCreatedEvent(
        String encounterId,
        String patientId) implements DomainEvent {

    @Override
    public String eventType() {
        return "encounter.created";
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
