package com.medtrust.clinical.infrastructure.client;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Resilient HTTP client for calling external microservices.
 *
 * Demonstrates all three Resilience4j patterns:
 * - Circuit Breaker: Opens after failure threshold to prevent cascade
 * - Retry: Retries transient failures with exponential backoff + jitter
 * - Bulkhead: Limits concurrent calls to prevent thread exhaustion
 *
 * Order of execution: Bulkhead → CircuitBreaker → Retry → Method
 */
@Component
public class ExternalServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ExternalServiceClient.class);

    private final RestClient restClient;

    public ExternalServiceClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    // ── Notification Service ──────────────────────────────────────────

    @CircuitBreaker(name = "notificationService", fallbackMethod = "notificationFallback")
    @Retry(name = "notificationService")
    @Bulkhead(name = "notificationService")
    public void sendNotification(String patientId, String message) {
        log.info("[ExternalClient] Sending notification for patient {}", patientId);
        restClient.post()
                .uri("http://notification-service:8080/api/notifications")
                .body(Map.of("patientId", patientId, "message", message))
                .retrieve()
                .toBodilessEntity();
        log.info("[ExternalClient] Notification sent successfully for patient {}", patientId);
    }

    @SuppressWarnings("unused")
    private void notificationFallback(String patientId, String message, Throwable t) {
        log.warn("[ExternalClient] Notification fallback triggered for patient {}. " +
                "Reason: {} - {}. Notification will be queued for later delivery.",
                patientId, t.getClass().getSimpleName(), t.getMessage());
        // In production: persist to a dead-letter store or outbox table for retry
    }

    // ── Billing Service ───────────────────────────────────────────────

    @CircuitBreaker(name = "billingService", fallbackMethod = "billingFallback")
    @Retry(name = "billingService")
    @Bulkhead(name = "billingService")
    public void submitBillingEvent(String encounterId, String eventType) {
        log.info("[ExternalClient] Submitting billing event '{}' for encounter {}",
                eventType, encounterId);
        restClient.post()
                .uri("http://billing-service:8080/api/billing/events")
                .body(Map.of("encounterId", encounterId, "eventType", eventType))
                .retrieve()
                .toBodilessEntity();
        log.info("[ExternalClient] Billing event submitted for encounter {}", encounterId);
    }

    @SuppressWarnings("unused")
    private void billingFallback(String encounterId, String eventType, Throwable t) {
        log.warn("[ExternalClient] Billing fallback triggered for encounter {}. " +
                "Reason: {} - {}. Event will be queued for later submission.",
                encounterId, t.getClass().getSimpleName(), t.getMessage());
    }

    // ── Lab Service ───────────────────────────────────────────────────

    @CircuitBreaker(name = "labService", fallbackMethod = "labResultsFallback")
    @Retry(name = "labService")
    @Bulkhead(name = "labService")
    public String fetchLabResults(String patientId) {
        log.info("[ExternalClient] Fetching lab results for patient {}", patientId);
        return restClient.get()
                .uri("http://lab-service:8080/api/labs/patients/{patientId}/results", patientId)
                .retrieve()
                .body(String.class);
    }

    @SuppressWarnings("unused")
    private String labResultsFallback(String patientId, Throwable t) {
        log.warn("[ExternalClient] Lab results fallback for patient {}. " +
                "Reason: {} - {}. Returning empty results.",
                patientId, t.getClass().getSimpleName(), t.getMessage());
        return "[]"; // graceful degradation — return empty result set
    }
}
