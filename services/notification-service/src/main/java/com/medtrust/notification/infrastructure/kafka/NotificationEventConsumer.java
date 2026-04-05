package com.medtrust.notification.infrastructure.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medtrust.notification.application.service.NotificationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka consumer — listens to domain events and auto-dispatches notifications.
 */
@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public NotificationEventConsumer(NotificationService notificationService,
                                     ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = {"auth-events", "appointment-events", "patient-events", "consent-events"},
            groupId = "notification-service-group")
    public void consumeEvent(ConsumerRecord<String, String> record) {
        try {
            String eventType = record.key();
            Map<String, Object> payload = objectMapper.readValue(record.value(), MAP_TYPE);

            log.debug("[NotificationConsumer] Processing '{}' from '{}'",
                    eventType, record.topic());

            notificationService.processEvent(eventType, payload);

        } catch (Exception e) {
            log.error("[NotificationConsumer] Failed to process from '{}': {}",
                    record.topic(), e.getMessage(), e);
        }
    }
}
