package com.medtrust.appointment.domain.event;

import java.time.Instant;
import java.util.Map;

public record AppointmentCancelledEvent(
        String appointmentId,
        String reason) implements DomainEvent {

    @Override
    public String eventType() {
        return "appointment.cancelled";
    }

    @Override
    public Instant occurredAt() {
        return Instant.now();
    }

    @Override
    public Map<String, Object> payload() {
        return Map.of(
                "appointmentId", appointmentId,
                "reason", reason);
    }
}
