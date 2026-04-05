package com.medtrust.integration.application.dto;

public record IntegrationMessageResponse(
        String id, String direction, String resourceType,
        String externalSystemId, String correlationId,
        String status, String errorMessage, String internalResourceId,
        String createdAt, String processedAt, String updatedAt) {
}
