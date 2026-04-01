package com.medtrust.patient.domain.event;

import java.time.Instant;
import java.util.Map;

public record PatientRegisteredEvent(
        String patientId,
        String mrn,
        String firstName,
        String lastName) implements DomainEvent {

    @Override
    public String eventType() {
        return "patient.registered";
    }

    @Override
    public Instant occurredAt() {
        return Instant.now();
    }

    @Override
    public Map<String, Object> payload() {
        return Map.of(
                "patientId", patientId,
                "mrn", mrn,
                "firstName", firstName,
                "lastName", lastName);
    }
}
