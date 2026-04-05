package com.medtrust.audit.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Immutable audit entry — once created, NEVER modified or deleted.
 * This is the core unit of the audit trail required for HIPAA compliance.
 *
 * Retention: 7+ years per HIPAA §164.530(j)
 */
public class AuditEntry {

    private final String id;
    private final String eventType;
    private final AuditCategory category;
    private final String sourceService;
    private final String actorId;
    private final String targetId;
    private final String targetType;
    private final Map<String, Object> payload;
    private final Instant eventTimestamp;
    private final Instant receivedAt;

    private AuditEntry(String id, String eventType, AuditCategory category,
                       String sourceService, String actorId,
                       String targetId, String targetType,
                       Map<String, Object> payload,
                       Instant eventTimestamp, Instant receivedAt) {
        this.id = id;
        this.eventType = eventType;
        this.category = category;
        this.sourceService = sourceService;
        this.actorId = actorId;
        this.targetId = targetId;
        this.targetType = targetType;
        this.payload = payload;
        this.eventTimestamp = eventTimestamp;
        this.receivedAt = receivedAt;
    }

    /**
     * Creates a new audit entry from a consumed Kafka event.
     */
    public static AuditEntry fromEvent(String eventType, String sourceService,
                                        Map<String, Object> payload) {
        return new AuditEntry(
                UUID.randomUUID().toString(),
                eventType,
                categorize(eventType),
                sourceService,
                extractString(payload, "userId", extractString(payload, "actorId", null)),
                extractString(payload, "patientId",
                        extractString(payload, "targetId",
                                extractString(payload, "consentId", null))),
                deriveTargetType(eventType),
                payload,
                Instant.now(),
                Instant.now());
    }

    /**
     * Reconstitute from persistence — read-only.
     */
    public static AuditEntry reconstitute(String id, String eventType, AuditCategory category,
                                           String sourceService, String actorId,
                                           String targetId, String targetType,
                                           Map<String, Object> payload,
                                           Instant eventTimestamp, Instant receivedAt) {
        return new AuditEntry(id, eventType, category, sourceService, actorId,
                targetId, targetType, payload, eventTimestamp, receivedAt);
    }

    // ── Event classification ──

    private static AuditCategory categorize(String eventType) {
        if (eventType == null) return AuditCategory.SYSTEM;
        return switch (eventType) {
            case "user.registered", "user.logged_in", "user.password_changed" -> AuditCategory.AUTHENTICATION;
            case "user.deactivated" -> AuditCategory.AUTHORIZATION;
            case "consent.granted", "consent.revoked" -> AuditCategory.CONSENT;
            case "patient.registered", "patient.contact_updated", "patient.deactivated" -> AuditCategory.PATIENT;
            case "encounter.created", "encounter.completed", "note.added" -> AuditCategory.CLINICAL;
            case "appointment.scheduled", "appointment.cancelled", "appointment.rescheduled",
                 "appointment.confirmed" -> AuditCategory.APPOINTMENT;
            default -> AuditCategory.SYSTEM;
        };
    }

    private static String deriveTargetType(String eventType) {
        if (eventType == null) return "UNKNOWN";
        if (eventType.startsWith("user.")) return "USER";
        if (eventType.startsWith("patient.")) return "PATIENT";
        if (eventType.startsWith("consent.")) return "CONSENT";
        if (eventType.startsWith("encounter.") || eventType.startsWith("note.")) return "ENCOUNTER";
        if (eventType.startsWith("appointment.")) return "APPOINTMENT";
        return "UNKNOWN";
    }

    private static String extractString(Map<String, Object> payload, String key, String fallback) {
        if (payload == null) return fallback;
        Object val = payload.get(key);
        return val != null ? val.toString() : fallback;
    }

    // ── Getters (no setters — immutable) ──

    public String getId() { return id; }
    public String getEventType() { return eventType; }
    public AuditCategory getCategory() { return category; }
    public String getSourceService() { return sourceService; }
    public String getActorId() { return actorId; }
    public String getTargetId() { return targetId; }
    public String getTargetType() { return targetType; }
    public Map<String, Object> getPayload() { return payload; }
    public Instant getEventTimestamp() { return eventTimestamp; }
    public Instant getReceivedAt() { return receivedAt; }
}
