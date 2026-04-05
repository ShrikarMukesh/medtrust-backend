package com.medtrust.integration.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Integration message aggregate — tracks every FHIR resource exchange
 * between MedTrust and external healthcare systems.
 */
public class IntegrationMessage {

    private final String id;
    private final IntegrationDirection direction;
    private final FhirResourceType resourceType;
    private final String externalSystemId;
    private final String correlationId;
    private final String fhirPayload;
    private IntegrationStatus status;
    private String errorMessage;
    private String internalResourceId;
    private final Instant createdAt;
    private Instant processedAt;
    private Instant updatedAt;

    private IntegrationMessage(String id, IntegrationDirection direction,
                                FhirResourceType resourceType, String externalSystemId,
                                String correlationId, String fhirPayload,
                                IntegrationStatus status, String errorMessage,
                                String internalResourceId,
                                Instant createdAt, Instant processedAt, Instant updatedAt) {
        this.id = id;
        this.direction = direction;
        this.resourceType = resourceType;
        this.externalSystemId = externalSystemId;
        this.correlationId = correlationId;
        this.fhirPayload = fhirPayload;
        this.status = status;
        this.errorMessage = errorMessage;
        this.internalResourceId = internalResourceId;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
        this.updatedAt = updatedAt;
    }

    public static IntegrationMessage createInbound(FhirResourceType resourceType,
                                                     String externalSystemId,
                                                     String fhirPayload) {
        return new IntegrationMessage(
                UUID.randomUUID().toString(),
                IntegrationDirection.INBOUND,
                resourceType, externalSystemId,
                UUID.randomUUID().toString(),
                fhirPayload,
                IntegrationStatus.PENDING,
                null, null,
                Instant.now(), null, Instant.now());
    }

    public static IntegrationMessage createOutbound(FhirResourceType resourceType,
                                                      String externalSystemId,
                                                      String internalResourceId,
                                                      String fhirPayload) {
        return new IntegrationMessage(
                UUID.randomUUID().toString(),
                IntegrationDirection.OUTBOUND,
                resourceType, externalSystemId,
                UUID.randomUUID().toString(),
                fhirPayload,
                IntegrationStatus.PENDING,
                null, internalResourceId,
                Instant.now(), null, Instant.now());
    }

    public static IntegrationMessage reconstitute(String id, IntegrationDirection direction,
                                                    FhirResourceType resourceType, String externalSystemId,
                                                    String correlationId, String fhirPayload,
                                                    IntegrationStatus status, String errorMessage,
                                                    String internalResourceId,
                                                    Instant createdAt, Instant processedAt, Instant updatedAt) {
        return new IntegrationMessage(id, direction, resourceType, externalSystemId,
                correlationId, fhirPayload, status, errorMessage, internalResourceId,
                createdAt, processedAt, updatedAt);
    }

    public void markProcessing() {
        this.status = IntegrationStatus.PROCESSING;
        this.updatedAt = Instant.now();
    }

    public void markCompleted(String internalResourceId) {
        this.status = IntegrationStatus.COMPLETED;
        this.internalResourceId = internalResourceId;
        this.processedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void markFailed(String error) {
        this.status = IntegrationStatus.FAILED;
        this.errorMessage = error;
        this.updatedAt = Instant.now();
    }

    public void markRejected(String reason) {
        this.status = IntegrationStatus.REJECTED;
        this.errorMessage = reason;
        this.updatedAt = Instant.now();
    }

    // ── Getters ──
    public String getId() { return id; }
    public IntegrationDirection getDirection() { return direction; }
    public FhirResourceType getResourceType() { return resourceType; }
    public String getExternalSystemId() { return externalSystemId; }
    public String getCorrelationId() { return correlationId; }
    public String getFhirPayload() { return fhirPayload; }
    public IntegrationStatus getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public String getInternalResourceId() { return internalResourceId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getProcessedAt() { return processedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
