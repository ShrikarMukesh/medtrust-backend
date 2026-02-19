package com.medtrust.clinical.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Resilience configuration for the clinical service.
 *
 * Configures:
 * - RestClient.Builder bean for ExternalServiceClient
 * - Circuit breaker state-transition event listeners for observability
 */
@Configuration
public class ResilienceConfig {

    private static final Logger log = LoggerFactory.getLogger(ResilienceConfig.class);

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    /**
     * Registers event listeners on all configured circuit breakers to log
     * state transitions (CLOSED → OPEN, OPEN → HALF_OPEN, HALF_OPEN → CLOSED).
     * This provides observability into circuit breaker behavior in production.
     */
    @PostConstruct
    public void registerCircuitBreakerEventListeners(CircuitBreakerRegistry registry) {
        registry.getAllCircuitBreakers().forEach(this::attachEventListener);

        // Also listen for dynamically created circuit breakers
        registry.getEventPublisher()
                .onEntryAdded(event -> attachEventListener(event.getAddedEntry()));
    }

    private void attachEventListener(CircuitBreaker cb) {
        cb.getEventPublisher()
                .onStateTransition(event -> log.warn("[CircuitBreaker] '{}' state transition: {} → {}",
                        event.getCircuitBreakerName(),
                        event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState()))
                .onFailureRateExceeded(event -> log.error("[CircuitBreaker] '{}' failure rate exceeded: {}%",
                        event.getCircuitBreakerName(),
                        event.getFailureRate()))
                .onSlowCallRateExceeded(event -> log.warn("[CircuitBreaker] '{}' slow call rate exceeded: {}%",
                        event.getCircuitBreakerName(),
                        event.getSlowCallRate()));
    }
}
