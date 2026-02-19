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
 * - RestClient.Builder bean for future external service clients
 * - Circuit breaker state-transition event listeners for observability
 */
@Configuration
public class ResilienceConfig {

        private static final Logger log = LoggerFactory.getLogger(ResilienceConfig.class);

        private final CircuitBreakerRegistry circuitBreakerRegistry;

        public ResilienceConfig(CircuitBreakerRegistry circuitBreakerRegistry) {
                this.circuitBreakerRegistry = circuitBreakerRegistry;
        }

        @Bean
        public RestClient.Builder restClientBuilder() {
                return RestClient.builder();
        }

        /**
         * Registers event listeners on all configured circuit breakers to log
         * state transitions (CLOSED → OPEN, HALF_OPEN → CLOSED, etc.).
         */
        @PostConstruct
        public void registerCircuitBreakerEventListeners() {
                circuitBreakerRegistry.getAllCircuitBreakers().forEach(this::attachEventListener);

                // Also listen for dynamically created circuit breakers
                circuitBreakerRegistry.getEventPublisher()
                                .onEntryAdded(event -> attachEventListener(event.getAddedEntry()));
        }

        private void attachEventListener(CircuitBreaker cb) {
                cb.getEventPublisher()
                                .onStateTransition(event -> log.warn("[CircuitBreaker] '{}' state transition: {} → {}",
                                                event.getCircuitBreakerName(),
                                                event.getStateTransition().getFromState(),
                                                event.getStateTransition().getToState()))
                                .onFailureRateExceeded(
                                                event -> log.error("[CircuitBreaker] '{}' failure rate exceeded: {}%",
                                                                event.getCircuitBreakerName(),
                                                                event.getFailureRate()))
                                .onSlowCallRateExceeded(
                                                event -> log.warn("[CircuitBreaker] '{}' slow call rate exceeded: {}%",
                                                                event.getCircuitBreakerName(),
                                                                event.getSlowCallRate()));
        }
}
