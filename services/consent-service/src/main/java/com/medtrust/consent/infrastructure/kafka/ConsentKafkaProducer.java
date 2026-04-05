package com.medtrust.consent.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medtrust.consent.domain.event.DomainEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ConsentKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(ConsentKafkaProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ConsentKafkaProducer(KafkaTemplate<String, String> kafkaTemplate,
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
        log.error("[KafkaProducer] FALLBACK — Failed to publish event '{}' to '{}': {}",
                event.eventType(), topic, t.getMessage());
    }
}
