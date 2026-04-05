package com.medtrust.audit.infrastructure.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medtrust.audit.application.service.AuditService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Core Kafka consumer — listens to ALL domain event topics from every service
 * and persists them as immutable audit entries.
 *
 * Topics consumed:
 * - auth-events      (user.registered, user.logged_in, etc.)
 * - consent-events   (consent.granted, consent.revoked)
 * - clinical-events  (encounter.created, note.added, etc.)
 * - appointment-events (appointment.scheduled, cancelled, etc.)
 * - patient-events   (patient.registered, contact_updated, etc.)
 */
@Component
public class AuditEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditEventConsumer.class);
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public AuditEventConsumer(AuditService auditService, ObjectMapper objectMapper) {
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = {"auth-events", "consent-events", "clinical-events",
                       "appointment-events", "patient-events"},
            groupId = "audit-service-group")
    public void consumeEvent(ConsumerRecord<String, String> record) {
        try {
            String eventType = record.key();
            String sourceService = deriveSourceService(record.topic());
            Map<String, Object> payload = objectMapper.readValue(record.value(), MAP_TYPE);

            auditService.recordEvent(eventType, sourceService, payload);

            log.debug("[AuditConsumer] Consumed event '{}' from topic '{}' (offset: {})",
                    eventType, record.topic(), record.offset());

        } catch (Exception e) {
            // Log but don't throw — we never want to block the consumer
            log.error("[AuditConsumer] Failed to process event from topic '{}' at offset {}: {}",
                    record.topic(), record.offset(), e.getMessage(), e);
        }
    }

    private String deriveSourceService(String topic) {
        return switch (topic) {
            case "auth-events" -> "auth-service";
            case "consent-events" -> "consent-service";
            case "clinical-events" -> "clinical-service";
            case "appointment-events" -> "appointment-service";
            case "patient-events" -> "patient-service";
            default -> "unknown-service";
        };
    }
}
