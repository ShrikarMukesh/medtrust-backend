package com.medtrust.audit.infrastructure.persistence.adapter;

import com.medtrust.audit.domain.model.AuditCategory;
import com.medtrust.audit.domain.model.AuditEntry;
import com.medtrust.audit.domain.repository.AuditEntryRepository;
import com.medtrust.audit.infrastructure.persistence.entity.AuditEntryJpaEntity;
import com.medtrust.audit.infrastructure.persistence.repository.AuditEntryJpaRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JpaAuditEntryRepositoryAdapter implements AuditEntryRepository {

    private final AuditEntryJpaRepository jpaRepository;

    public JpaAuditEntryRepositoryAdapter(AuditEntryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AuditEntry save(AuditEntry entry) {
        return toDomain(jpaRepository.save(toEntity(entry)));
    }

    @Override
    public Optional<AuditEntry> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id)).map(this::toDomain);
    }

    @Override
    public List<AuditEntry> findByCategory(AuditCategory category) {
        return jpaRepository.findByCategory(category.name()).stream().map(this::toDomain).toList();
    }

    @Override
    public List<AuditEntry> findBySourceService(String sourceService) {
        return jpaRepository.findBySourceService(sourceService).stream().map(this::toDomain).toList();
    }

    @Override
    public List<AuditEntry> findByActorId(String actorId) {
        return jpaRepository.findByActorId(actorId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<AuditEntry> findByTargetId(String targetId) {
        return jpaRepository.findByTargetId(targetId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<AuditEntry> findByEventType(String eventType) {
        return jpaRepository.findByEventType(eventType).stream().map(this::toDomain).toList();
    }

    @Override
    public List<AuditEntry> findByEventTimestampBetween(Instant from, Instant to) {
        return jpaRepository.findByEventTimestampBetween(from, to).stream().map(this::toDomain).toList();
    }

    @Override
    public List<AuditEntry> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    private AuditEntryJpaEntity toEntity(AuditEntry e) {
        AuditEntryJpaEntity entity = new AuditEntryJpaEntity();
        entity.setId(UUID.fromString(e.getId()));
        entity.setEventType(e.getEventType());
        entity.setCategory(e.getCategory().name());
        entity.setSourceService(e.getSourceService());
        entity.setActorId(e.getActorId());
        entity.setTargetId(e.getTargetId());
        entity.setTargetType(e.getTargetType());
        entity.setPayload(e.getPayload());
        entity.setEventTimestamp(e.getEventTimestamp());
        entity.setReceivedAt(e.getReceivedAt());
        return entity;
    }

    private AuditEntry toDomain(AuditEntryJpaEntity e) {
        return AuditEntry.reconstitute(
                e.getId().toString(), e.getEventType(),
                AuditCategory.valueOf(e.getCategory()),
                e.getSourceService(), e.getActorId(),
                e.getTargetId(), e.getTargetType(),
                e.getPayload(), e.getEventTimestamp(), e.getReceivedAt());
    }
}
