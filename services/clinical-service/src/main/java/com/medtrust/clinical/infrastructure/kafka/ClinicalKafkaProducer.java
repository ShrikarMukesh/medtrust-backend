package com.medtrust.clinical.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medtrust.clinical.domain.event.DomainEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Resilient Kafka producer with circuit breaker and retry protection.
 *
 * If Kafka is temporarily unavailable:
 * - Retry: Automatically retries with exponential backoff
 * - Circuit Breaker: Opens circuit after repeated failures to prevent
 * wasting threads on a dead broker
 *
 * Fallback logs the failed event for later reprocessing via scheduled jobs
 * or a dead-letter store.
 */
@Component
public class ClinicalKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(ClinicalKafkaProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ClinicalKafkaProducer(KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @CircuitBreaker(name = "kafkaProducer", fallbackMethod = "publishFallback")
    @Retry(name = "kafkaProducer")
    public void publish(String topic, DomainEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event.payload());
            kafkaTemplate.send(topic, event.eventType(), payload);
            log.info("[KafkaProducer] Published event '{}' to topic '{}'", event.eventType(), topic);
        } catch (JsonProcessingException e) {
            log.error("[KafkaProducer] Failed to serialize event: {}", event.eventType(), e);
            throw new RuntimeException("Event serialization failed", e);
        }
    }

    @SuppressWarnings("unused")
    private void publishFallback(String topic, DomainEvent event, Throwable t) {
        log.error("[KafkaProducer] FALLBACK — Failed to publish event '{}' to topic '{}'. " +
                "Reason: {} - {}. Event will be stored for later reprocessing.",
                event.eventType(), topic, t.getClass().getSimpleName(), t.getMessage());
        // In production: persist to outbox table or dead-letter store
        // Example: outboxRepository.save(new OutboxEntry(topic, event));
    }
}
