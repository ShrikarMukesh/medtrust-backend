package com.medtrust.integration.application.service;

import com.medtrust.integration.application.dto.*;
import com.medtrust.integration.domain.model.*;
import com.medtrust.integration.domain.repository.IntegrationMessageRepository;
import com.medtrust.integration.infrastructure.fhir.FhirValidator;
import com.medtrust.integration.infrastructure.kafka.IntegrationKafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class IntegrationService {

    private static final Logger log = LoggerFactory.getLogger(IntegrationService.class);

    private final IntegrationMessageRepository repository;
    private final FhirValidator fhirValidator;
    private final IntegrationKafkaProducer kafkaProducer;

    public IntegrationService(IntegrationMessageRepository repository,
                               FhirValidator fhirValidator,
                               IntegrationKafkaProducer kafkaProducer) {
        this.repository = repository;
        this.fhirValidator = fhirValidator;
        this.kafkaProducer = kafkaProducer;
    }

    /**
     * Process an inbound FHIR resource from an external system.
     */
    public IntegrationMessageResponse processInbound(InboundFhirRequest request) {
        FhirResourceType resourceType = FhirResourceType.valueOf(
                request.resourceType().toUpperCase());

        IntegrationMessage message = IntegrationMessage.createInbound(
                resourceType, request.externalSystemId(), request.fhirPayload());
        message = repository.save(message);

        // Validate FHIR payload
        message.markProcessing();
        var validationResult = fhirValidator.validate(request.fhirPayload());

        if (!validationResult.isValid()) {
            message.markRejected("FHIR validation failed: " + validationResult.errors());
            repository.save(message);
            log.warn("[Integration] Inbound {} rejected from '{}': {}",
                    resourceType, request.externalSystemId(), validationResult.errors());
            return toResponse(message);
        }

        // For now, mark as completed. In production, this would trigger
        // a call to the appropriate internal service (patient-service, etc.)
        message.markCompleted("pending-internal-mapping");
        repository.save(message);

        kafkaProducer.publish("integration-events", "fhir.inbound.received",
                Map.of("correlationId", message.getCorrelationId(),
                        "resourceType", resourceType.name(),
                        "externalSystem", request.externalSystemId()));

        log.info("[Integration] Inbound {} from '{}' processed (correlation: {})",
                resourceType, request.externalSystemId(), message.getCorrelationId());

        return toResponse(message);
    }

    /**
     * Export an internal resource as FHIR R4 to an external system.
     */
    public IntegrationMessageResponse processOutbound(OutboundFhirRequest request) {
        FhirResourceType resourceType = FhirResourceType.valueOf(
                request.resourceType().toUpperCase());

        // In production, this would call the internal service, convert to FHIR,
        // then send to the external system. For now, we generate a stub.
        String fhirPayload = generateFhirStub(resourceType, request.internalResourceId());

        IntegrationMessage message = IntegrationMessage.createOutbound(
                resourceType, request.externalSystemId(),
                request.internalResourceId(), fhirPayload);

        message.markProcessing();
        message.markCompleted(request.internalResourceId());
        message = repository.save(message);

        kafkaProducer.publish("integration-events", "fhir.outbound.sent",
                Map.of("correlationId", message.getCorrelationId(),
                        "resourceType", resourceType.name(),
                        "externalSystem", request.externalSystemId()));

        log.info("[Integration] Outbound {} to '{}' sent (correlation: {})",
                resourceType, request.externalSystemId(), message.getCorrelationId());

        return toResponse(message);
    }

    @Transactional(readOnly = true)
    public IntegrationMessageResponse findById(String id) {
        return repository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Integration message not found: " + id));
    }

    @Transactional(readOnly = true)
    public IntegrationMessageResponse findByCorrelationId(String correlationId) {
        return repository.findByCorrelationId(correlationId).map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Correlation ID not found: " + correlationId));
    }

    @Transactional(readOnly = true)
    public List<IntegrationMessageResponse> findByDirection(String direction) {
        return repository.findByDirection(IntegrationDirection.valueOf(direction.toUpperCase()))
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<IntegrationMessageResponse> findByStatus(String status) {
        return repository.findByStatus(IntegrationStatus.valueOf(status.toUpperCase()))
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<IntegrationMessageResponse> findByExternalSystem(String systemId) {
        return repository.findByExternalSystemId(systemId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<IntegrationMessageResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    private String generateFhirStub(FhirResourceType type, String resourceId) {
        return String.format("""
                {"resourceType": "%s", "id": "%s", "meta": {"profile": ["http://hl7.org/fhir/R4"]}}
                """, type.name().substring(0, 1) + type.name().substring(1).toLowerCase(), resourceId);
    }

    private IntegrationMessageResponse toResponse(IntegrationMessage m) {
        return new IntegrationMessageResponse(
                m.getId(), m.getDirection().name(), m.getResourceType().name(),
                m.getExternalSystemId(), m.getCorrelationId(),
                m.getStatus().name(), m.getErrorMessage(), m.getInternalResourceId(),
                m.getCreatedAt().toString(),
                m.getProcessedAt() != null ? m.getProcessedAt().toString() : null,
                m.getUpdatedAt().toString());
    }
}
