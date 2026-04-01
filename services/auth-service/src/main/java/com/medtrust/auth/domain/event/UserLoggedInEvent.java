package com.medtrust.auth.domain.event;

import java.time.Instant;
import java.util.Map;

public record UserLoggedInEvent(String userId, String email) implements DomainEvent {
    @Override public String eventType() { return "user.logged_in"; }
    @Override public Instant occurredAt() { return Instant.now(); }
    @Override public Map<String, Object> payload() {
        return Map.of("userId", userId, "email", email);
    }
}
