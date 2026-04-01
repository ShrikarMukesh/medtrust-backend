package com.medtrust.auth.domain.event;

import java.time.Instant;
import java.util.Map;

public record UserRegisteredEvent(String userId, String email, String role) implements DomainEvent {
    @Override public String eventType() { return "user.registered"; }
    @Override public Instant occurredAt() { return Instant.now(); }
    @Override public Map<String, Object> payload() {
        return Map.of("userId", userId, "email", email, "role", role);
    }
}
