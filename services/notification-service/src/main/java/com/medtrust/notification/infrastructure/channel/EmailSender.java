package com.medtrust.notification.infrastructure.channel;

import com.medtrust.notification.application.port.NotificationSender;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Email sender via Spring Boot Mail (SMTP).
 */
@Component
public class EmailSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(EmailSender.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final boolean enabled;

    public EmailSender(JavaMailSender mailSender,
                       @Value("${app.email.from:noreply@medtrust.com}") String fromAddress,
                       @Value("${app.email.enabled:false}") boolean enabled) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.enabled = enabled;
    }

    @Override
    public String channel() { return "EMAIL"; }

    @Override
    @CircuitBreaker(name = "emailSender", fallbackMethod = "sendFallback")
    @Retry(name = "emailSender")
    public String send(String recipientEmail, String subject, String body) {
        if (!enabled) {
            String mockId = "email-mock-" + UUID.randomUUID().toString().substring(0, 8);
            log.info("[Email] MOCK mode — would send to {}: subject='{}', body='{}'",
                    recipientEmail, subject, body);
            return mockId;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipientEmail);
        message.setSubject(subject != null ? subject : "MedTrust Notification");
        message.setText(body);

        mailSender.send(message);

        String messageId = "email-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("[Email] Sent to {}, subject: '{}'", recipientEmail, subject);
        return messageId;
    }

    @SuppressWarnings("unused")
    private String sendFallback(String email, String subject, String body, Throwable t) {
        log.error("[Email] FALLBACK — Failed to send to {}: {}", email, t.getMessage());
        throw new RuntimeException("Email delivery failed: " + t.getMessage(), t);
    }
}
