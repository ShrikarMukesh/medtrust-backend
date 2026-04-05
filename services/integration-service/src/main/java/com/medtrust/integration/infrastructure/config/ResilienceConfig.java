package com.medtrust.integration.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceConfig {
    private static final Logger log = LoggerFactory.getLogger(ResilienceConfig.class);
    private final CircuitBreakerRegistry registry;
    public ResilienceConfig(CircuitBreakerRegistry registry) { this.registry = registry; }

    @PostConstruct
    public void init() {
        registry.getAllCircuitBreakers().forEach(cb ->
                cb.getEventPublisher().onStateTransition(e ->
                        log.warn("[CB] '{}': {} → {}", e.getCircuitBreakerName(),
                                e.getStateTransition().getFromState(),
                                e.getStateTransition().getToState())));
    }
}
