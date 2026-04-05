package com.medtrust.notification.infrastructure.channel;

import com.medtrust.notification.application.port.NotificationSender;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

/**
 * WhatsApp Cloud API sender.
 *
 * Uses Meta Graph API v21.0:
 * POST https://graph.facebook.com/v21.0/{phone_number_id}/messages
 *
 * Required env vars:
 * - WHATSAPP_PHONE_NUMBER_ID  (your business phone number ID)
 * - WHATSAPP_ACCESS_TOKEN     (Meta API access token)
 */
@Component
public class WhatsAppSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppSender.class);

    private final RestClient restClient;
    private final String phoneNumberId;
    private final String accessToken;
    private final boolean enabled;

    public WhatsAppSender(RestClient.Builder restClientBuilder,
                          @Value("${app.whatsapp.phone-number-id:}") String phoneNumberId,
                          @Value("${app.whatsapp.access-token:}") String accessToken,
                          @Value("${app.whatsapp.enabled:false}") boolean enabled) {
        this.restClient = restClientBuilder.build();
        this.phoneNumberId = phoneNumberId;
        this.accessToken = accessToken;
        this.enabled = enabled;
    }

    @Override
    public String channel() { return "WHATSAPP"; }

    @Override
    @CircuitBreaker(name = "whatsappSender", fallbackMethod = "sendFallback")
    @Retry(name = "whatsappSender")
    public String send(String recipientPhone, String subject, String body) {
        if (!enabled) {
            String mockId = "wa-mock-" + UUID.randomUUID().toString().substring(0, 8);
            log.info("[WhatsApp] MOCK mode — would send to {}: {}", recipientPhone, body);
            return mockId;
        }

        String url = String.format("https://graph.facebook.com/v21.0/%s/messages", phoneNumberId);

        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", recipientPhone,
                "type", "text",
                "text", Map.of("body", body));

        String response = restClient.post()
                .uri(url)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(String.class);

        log.info("[WhatsApp] Sent to {}, response: {}", recipientPhone, response);
        return "wa-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @SuppressWarnings("unused")
    private String sendFallback(String phone, String subject, String body, Throwable t) {
        log.error("[WhatsApp] FALLBACK — Failed to send to {}: {}", phone, t.getMessage());
        throw new RuntimeException("WhatsApp delivery failed: " + t.getMessage(), t);
    }
}
