package com.medtrust.audit.application.service;

import com.medtrust.audit.application.dto.AuditEntryResponse;
import com.medtrust.audit.domain.model.AuditCategory;
import com.medtrust.audit.domain.model.AuditEntry;
import com.medtrust.audit.domain.repository.AuditEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditEntryRepository auditEntryRepository;

    public AuditService(AuditEntryRepository auditEntryRepository) {
        this.auditEntryRepository = auditEntryRepository;
    }

    /**
     * Record an audit entry from a consumed Kafka event.
     */
    public void recordEvent(String eventType, String sourceService, Map<String, Object> payload) {
        AuditEntry entry = AuditEntry.fromEvent(eventType, sourceService, payload);
        auditEntryRepository.save(entry);
        log.info("[AuditService] Recorded '{}' from '{}' → category: {}",
                eventType, sourceService, entry.getCategory());
    }

    @Transactional(readOnly = true)
    public AuditEntryResponse findById(String id) {
        return auditEntryRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Audit entry not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<AuditEntryResponse> findByCategory(String category) {
        AuditCategory cat = AuditCategory.valueOf(category.toUpperCase());
        return auditEntryRepository.findByCategory(cat).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AuditEntryResponse> findBySourceService(String sourceService) {
        return auditEntryRepository.findBySourceService(sourceService).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AuditEntryResponse> findByActorId(String actorId) {
        return auditEntryRepository.findByActorId(actorId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AuditEntryResponse> findByTargetId(String targetId) {
        return auditEntryRepository.findByTargetId(targetId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AuditEntryResponse> findByEventType(String eventType) {
        return auditEntryRepository.findByEventType(eventType).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AuditEntryResponse> findByDateRange(String from, String to) {
        Instant fromInstant = Instant.parse(from);
        Instant toInstant = Instant.parse(to);
        return auditEntryRepository.findByEventTimestampBetween(fromInstant, toInstant).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AuditEntryResponse> findAll() {
        return auditEntryRepository.findAll().stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public long count() {
        return auditEntryRepository.count();
    }

    private AuditEntryResponse toResponse(AuditEntry entry) {
        return new AuditEntryResponse(
                entry.getId(),
                entry.getEventType(),
                entry.getCategory().name(),
                entry.getSourceService(),
                entry.getActorId(),
                entry.getTargetId(),
                entry.getTargetType(),
                entry.getPayload(),
                entry.getEventTimestamp().toString(),
                entry.getReceivedAt().toString());
    }
}
