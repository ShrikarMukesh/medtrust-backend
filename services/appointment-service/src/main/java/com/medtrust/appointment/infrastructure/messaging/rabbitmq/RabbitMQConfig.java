package com.medtrust.appointment.infrastructure.messaging.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String SMS_NOTIFICATION_QUEUE = "sms.notification.queue";
    public static final String SMS_ROUTING_KEY = "sms.routing.key";

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Queue smsNotificationQueue() {
        return new Queue(SMS_NOTIFICATION_QUEUE, true);
    }

    @Bean
    public Binding smsBinding(Queue smsNotificationQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(smsNotificationQueue)
                .to(notificationExchange)
                .with(SMS_ROUTING_KEY);
    }
}
