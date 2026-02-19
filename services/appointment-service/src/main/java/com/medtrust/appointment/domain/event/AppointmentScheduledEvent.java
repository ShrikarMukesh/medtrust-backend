package com.medtrust.appointment.domain.event;

import java.time.Instant;
import java.util.Map;

public record AppointmentScheduledEvent(
        String appointmentId,
        String patientId,
        String providerId,
        Instant startTime) implements DomainEvent {

    @Override
    public String eventType() {
        return "appointment.scheduled";
    }

    @Override
    public Instant occurredAt() {
        return Instant.now();
    }

    @Override
    public Map<String, Object> payload() {
        return Map.of(
                "appointmentId", appointmentId,
                "patientId", patientId,
                "providerId", providerId,
                "startTime", startTime.toString());
    }
}
