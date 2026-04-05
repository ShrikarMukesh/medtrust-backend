package com.medtrust.audit.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceConfig {

    private static final Logger log = LoggerFactory.getLogger(ResilienceConfig.class);
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public ResilienceConfig(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @PostConstruct
    public void registerCircuitBreakerEventListeners() {
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(this::attachEventListener);
        circuitBreakerRegistry.getEventPublisher()
                .onEntryAdded(event -> attachEventListener(event.getAddedEntry()));
    }

    private void attachEventListener(CircuitBreaker cb) {
        cb.getEventPublisher()
                .onStateTransition(event -> log.warn("[CircuitBreaker] '{}' transition: {} → {}",
                        event.getCircuitBreakerName(),
                        event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState()));
    }
}
