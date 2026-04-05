package com.medtrust.audit.domain.repository;

import com.medtrust.audit.domain.model.AuditCategory;
import com.medtrust.audit.domain.model.AuditEntry;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AuditEntryRepository {
    AuditEntry save(AuditEntry entry);
    Optional<AuditEntry> findById(String id);
    List<AuditEntry> findByCategory(AuditCategory category);
    List<AuditEntry> findBySourceService(String sourceService);
    List<AuditEntry> findByActorId(String actorId);
    List<AuditEntry> findByTargetId(String targetId);
    List<AuditEntry> findByEventType(String eventType);
    List<AuditEntry> findByEventTimestampBetween(Instant from, Instant to);
    List<AuditEntry> findAll();
    long count();
}
