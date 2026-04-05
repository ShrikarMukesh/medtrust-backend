package com.medtrust.integration.domain.repository;

import com.medtrust.integration.domain.model.IntegrationDirection;
import com.medtrust.integration.domain.model.IntegrationMessage;
import com.medtrust.integration.domain.model.IntegrationStatus;
import java.util.List;
import java.util.Optional;

public interface IntegrationMessageRepository {
    IntegrationMessage save(IntegrationMessage message);
    Optional<IntegrationMessage> findById(String id);
    Optional<IntegrationMessage> findByCorrelationId(String correlationId);
    List<IntegrationMessage> findByDirection(IntegrationDirection direction);
    List<IntegrationMessage> findByStatus(IntegrationStatus status);
    List<IntegrationMessage> findByExternalSystemId(String externalSystemId);
    List<IntegrationMessage> findAll();
    long count();
}
