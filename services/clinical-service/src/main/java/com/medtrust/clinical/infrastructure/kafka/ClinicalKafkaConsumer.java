package com.medtrust.clinical.infrastructure.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ClinicalKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(ClinicalKafkaConsumer.class);

    // Placeholder for future event consumption.
    // Uncomment and configure topics as needed:
    //
    // @KafkaListener(topics = "clinical-events", groupId =
    // "${spring.kafka.consumer.group-id}")
    // public void consume(String message) {
    // log.info("[KafkaConsumer] Received message: {}", message);
    // }
}
