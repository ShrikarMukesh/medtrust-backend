package com.medtrust.appointment.infrastructure.messaging.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendSmsNotification(String phoneNumber, String message) {
        log.info("Sending SMS notification via RabbitMQ to: {}", phoneNumber);

        // In a real application, you'd serialize this to JSON
        String payload = String.format("{\"phoneNumber\": \"%s\", \"message\": \"%s\"}", phoneNumber, message);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.SMS_ROUTING_KEY,
                payload);

        log.debug("SMS notification payload sent: {}", payload);
    }
}
