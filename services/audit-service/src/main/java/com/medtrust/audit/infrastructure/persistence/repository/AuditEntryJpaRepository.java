package com.medtrust.audit.infrastructure.persistence.repository;

import com.medtrust.audit.infrastructure.persistence.entity.AuditEntryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditEntryJpaRepository extends JpaRepository<AuditEntryJpaEntity, UUID> {
    List<AuditEntryJpaEntity> findByCategory(String category);
    List<AuditEntryJpaEntity> findBySourceService(String sourceService);
    List<AuditEntryJpaEntity> findByActorId(String actorId);
    List<AuditEntryJpaEntity> findByTargetId(String targetId);
    List<AuditEntryJpaEntity> findByEventType(String eventType);
    List<AuditEntryJpaEntity> findByEventTimestampBetween(Instant from, Instant to);
}
