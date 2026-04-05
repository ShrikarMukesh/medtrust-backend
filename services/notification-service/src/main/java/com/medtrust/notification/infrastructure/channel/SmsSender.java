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
 * SMS sender via Twilio REST API.
 *
 * Required env vars:
 * - TWILIO_ACCOUNT_SID
 * - TWILIO_AUTH_TOKEN
 * - TWILIO_FROM_NUMBER
 */
@Component
public class SmsSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(SmsSender.class);

    private final RestClient restClient;
    private final String accountSid;
    private final String authToken;
    private final String fromNumber;
    private final boolean enabled;

    public SmsSender(RestClient.Builder restClientBuilder,
                     @Value("${app.sms.twilio.account-sid:}") String accountSid,
                     @Value("${app.sms.twilio.auth-token:}") String authToken,
                     @Value("${app.sms.twilio.from-number:}") String fromNumber,
                     @Value("${app.sms.enabled:false}") boolean enabled) {
        this.restClient = restClientBuilder.build();
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.fromNumber = fromNumber;
        this.enabled = enabled;
    }

    @Override
    public String channel() { return "SMS"; }

    @Override
    @CircuitBreaker(name = "smsSender", fallbackMethod = "sendFallback")
    @Retry(name = "smsSender")
    public String send(String recipientPhone, String subject, String body) {
        if (!enabled) {
            String mockId = "sms-mock-" + UUID.randomUUID().toString().substring(0, 8);
            log.info("[SMS] MOCK mode — would send to {}: {}", recipientPhone, body);
            return mockId;
        }

        String url = String.format(
                "https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json", accountSid);

        String response = restClient.post()
                .uri(url)
                .headers(h -> h.setBasicAuth(accountSid, authToken))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(String.format("From=%s&To=%s&Body=%s", fromNumber, recipientPhone, body))
                .retrieve()
                .body(String.class);

        log.info("[SMS] Sent to {}, response: {}", recipientPhone, response);
        return "sms-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @SuppressWarnings("unused")
    private String sendFallback(String phone, String subject, String body, Throwable t) {
        log.error("[SMS] FALLBACK — Failed to send to {}: {}", phone, t.getMessage());
        throw new RuntimeException("SMS delivery failed: " + t.getMessage(), t);
    }
}
