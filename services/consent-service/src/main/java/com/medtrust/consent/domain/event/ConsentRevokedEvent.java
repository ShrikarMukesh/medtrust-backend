package com.medtrust.consent.domain.event;

import java.time.Instant;
import java.util.Map;

public record ConsentRevokedEvent(
        String consentId, String patientId,
        String grantedToUserId) implements DomainEvent {
    @Override public String eventType() { return "consent.revoked"; }
    @Override public Instant occurredAt() { return Instant.now(); }
    @Override public Map<String, Object> payload() {
        return Map.of("consentId", consentId, "patientId", patientId,
                "grantedToUserId", grantedToUserId);
    }
}
