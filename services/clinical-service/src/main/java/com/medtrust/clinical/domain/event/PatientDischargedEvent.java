package com.medtrust.clinical.domain.event;

import java.time.Instant;
import java.util.Map;

public record PatientDischargedEvent(
        String encounterId,
        String patientId,
        Instant dischargeDate) implements DomainEvent {

    @Override
    public String eventType() {
        return "patient.discharged";
    }

    @Override
    public Instant occurredAt() {
        return Instant.now();
    }

    @Override
    public Map<String, Object> payload() {
        return Map.of(
                "encounterId", encounterId,
                "patientId", patientId,
                "dischargeDate", dischargeDate.toString());
    }
}
