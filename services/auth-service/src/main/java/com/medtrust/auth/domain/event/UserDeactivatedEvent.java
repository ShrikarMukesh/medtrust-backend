package com.medtrust.auth.domain.event;

import java.time.Instant;
import java.util.Map;

public record UserDeactivatedEvent(String userId, String email) implements DomainEvent {
    @Override public String eventType() { return "user.deactivated"; }
    @Override public Instant occurredAt() { return Instant.now(); }
    @Override public Map<String, Object> payload() {
        return Map.of("userId", userId, "email", email);
    }
}
