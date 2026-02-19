package com.medtrust.appointment.domain.event;

import java.time.Instant;
import java.util.Map;

public record AppointmentRescheduledEvent(
        String appointmentId,
        Instant oldStartTime,
        Instant newStartTime) implements DomainEvent {

    @Override
    public String eventType() {
        return "appointment.rescheduled";
    }

    @Override
    public Instant occurredAt() {
        return Instant.now();
    }

    @Override
    public Map<String, Object> payload() {
        return Map.of(
                "appointmentId", appointmentId,
                "oldStartTime", oldStartTime.toString(),
                "newStartTime", newStartTime.toString());
    }
}
