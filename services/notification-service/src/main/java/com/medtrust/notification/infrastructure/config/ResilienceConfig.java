package com.medtrust.notification.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ResilienceConfig {

    private static final Logger log = LoggerFactory.getLogger(ResilienceConfig.class);
    private final CircuitBreakerRegistry registry;

    public ResilienceConfig(CircuitBreakerRegistry registry) { this.registry = registry; }

    @Bean
    public RestClient.Builder restClientBuilder() { return RestClient.builder(); }

    @PostConstruct
    public void init() {
        registry.getAllCircuitBreakers().forEach(this::listen);
        registry.getEventPublisher().onEntryAdded(e -> listen(e.getAddedEntry()));
    }

    private void listen(CircuitBreaker cb) {
        cb.getEventPublisher().onStateTransition(e ->
                log.warn("[CB] '{}': {} → {}", e.getCircuitBreakerName(),
                        e.getStateTransition().getFromState(),
                        e.getStateTransition().getToState()));
    }
}
