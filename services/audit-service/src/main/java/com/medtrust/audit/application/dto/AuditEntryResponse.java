package com.medtrust.audit.application.dto;

import java.util.Map;

public record AuditEntryResponse(
        String id,
        String eventType,
        String category,
        String sourceService,
        String actorId,
        String targetId,
        String targetType,
        Map<String, Object> payload,
        String eventTimestamp,
        String receivedAt) {
}
