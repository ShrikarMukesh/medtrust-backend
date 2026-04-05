package com.medtrust.integration.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "integration_messages")
public class IntegrationMessageJpaEntity {
    @Id @Column(columnDefinition = "uuid") private UUID id;
    @Column(nullable = false, length = 20) private String direction;
    @Column(name = "resource_type", nullable = false, length = 50) private String resourceType;
    @Column(name = "external_system_id", nullable = false, length = 100) private String externalSystemId;
    @Column(name = "correlation_id", nullable = false, unique = true, length = 100) private String correlationId;
    @Column(name = "fhir_payload", columnDefinition = "TEXT") private String fhirPayload;
    @Column(nullable = false, length = 20) private String status;
    @Column(name = "error_message", length = 500) private String errorMessage;
    @Column(name = "internal_resource_id", length = 100) private String internalResourceId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "processed_at") private Instant processedAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    public IntegrationMessageJpaEntity() {}
    public UUID getId() { return id; } public void setId(UUID id) { this.id = id; }
    public String getDirection() { return direction; } public void setDirection(String d) { this.direction = d; }
    public String getResourceType() { return resourceType; } public void setResourceType(String t) { this.resourceType = t; }
    public String getExternalSystemId() { return externalSystemId; } public void setExternalSystemId(String s) { this.externalSystemId = s; }
    public String getCorrelationId() { return correlationId; } public void setCorrelationId(String c) { this.correlationId = c; }
    public String getFhirPayload() { return fhirPayload; } public void setFhirPayload(String p) { this.fhirPayload = p; }
    public String getStatus() { return status; } public void setStatus(String s) { this.status = s; }
    public String getErrorMessage() { return errorMessage; } public void setErrorMessage(String e) { this.errorMessage = e; }
    public String getInternalResourceId() { return internalResourceId; } public void setInternalResourceId(String r) { this.internalResourceId = r; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant t) { this.createdAt = t; }
    public Instant getProcessedAt() { return processedAt; } public void setProcessedAt(Instant t) { this.processedAt = t; }
    public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant t) { this.updatedAt = t; }
}
