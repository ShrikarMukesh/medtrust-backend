package com.medtrust.clinical.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medtrust.clinical.domain.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

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

    public void publish(String topic, DomainEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event.payload());
            kafkaTemplate.send(topic, event.eventType(), payload);
            log.info("[KafkaProducer] Published event '{}' to topic '{}'", event.eventType(), topic);
        } catch (JsonProcessingException e) {
            log.error("[KafkaProducer] Failed to serialize event: {}", event.eventType(), e);
        }
    }
}
