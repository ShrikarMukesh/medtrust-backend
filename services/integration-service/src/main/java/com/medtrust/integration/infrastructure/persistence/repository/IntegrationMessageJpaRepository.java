package com.medtrust.integration.infrastructure.persistence.repository;

import com.medtrust.integration.infrastructure.persistence.entity.IntegrationMessageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IntegrationMessageJpaRepository extends JpaRepository<IntegrationMessageJpaEntity, UUID> {
    Optional<IntegrationMessageJpaEntity> findByCorrelationId(String correlationId);
    List<IntegrationMessageJpaEntity> findByDirection(String direction);
    List<IntegrationMessageJpaEntity> findByStatus(String status);
    List<IntegrationMessageJpaEntity> findByExternalSystemId(String externalSystemId);
}
