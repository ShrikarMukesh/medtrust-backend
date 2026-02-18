package com.medtrust.clinical.domain.event;

import java.time.Instant;
import java.util.Map;

public interface DomainEvent {
    String eventType();

    Instant occurredAt();

    Map<String, Object> payload();
}
