package com.medtrust.integration.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class IntegrationKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(IntegrationKafkaProducer.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public IntegrationKafkaProducer(KafkaTemplate<String, String> kafkaTemplate,
                                     ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @CircuitBreaker(name = "kafkaProducer", fallbackMethod = "fallback")
    @Retry(name = "kafkaProducer")
    public void publish(String topic, String eventType, Map<String, Object> payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, eventType, json);
            log.info("[Kafka] Published '{}' to '{}'", eventType, topic);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Serialization failed", e);
        }
    }

    @SuppressWarnings("unused")
    private void fallback(String topic, String eventType, Map<String, Object> payload, Throwable t) {
        log.error("[Kafka] FALLBACK — Failed to publish '{}' to '{}': {}", eventType, topic, t.getMessage());
    }
}
