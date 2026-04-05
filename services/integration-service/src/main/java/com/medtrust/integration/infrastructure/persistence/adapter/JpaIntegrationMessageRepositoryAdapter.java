package com.medtrust.integration.infrastructure.persistence.adapter;

import com.medtrust.integration.domain.model.*;
import com.medtrust.integration.domain.repository.IntegrationMessageRepository;
import com.medtrust.integration.infrastructure.persistence.entity.IntegrationMessageJpaEntity;
import com.medtrust.integration.infrastructure.persistence.repository.IntegrationMessageJpaRepository;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class JpaIntegrationMessageRepositoryAdapter implements IntegrationMessageRepository {

    private final IntegrationMessageJpaRepository jpa;
    public JpaIntegrationMessageRepositoryAdapter(IntegrationMessageJpaRepository jpa) { this.jpa = jpa; }

    @Override public IntegrationMessage save(IntegrationMessage m) { return toDomain(jpa.save(toEntity(m))); }
    @Override public Optional<IntegrationMessage> findById(String id) { return jpa.findById(UUID.fromString(id)).map(this::toDomain); }
    @Override public Optional<IntegrationMessage> findByCorrelationId(String cid) { return jpa.findByCorrelationId(cid).map(this::toDomain); }
    @Override public List<IntegrationMessage> findByDirection(IntegrationDirection d) { return jpa.findByDirection(d.name()).stream().map(this::toDomain).toList(); }
    @Override public List<IntegrationMessage> findByStatus(IntegrationStatus s) { return jpa.findByStatus(s.name()).stream().map(this::toDomain).toList(); }
    @Override public List<IntegrationMessage> findByExternalSystemId(String sid) { return jpa.findByExternalSystemId(sid).stream().map(this::toDomain).toList(); }
    @Override public List<IntegrationMessage> findAll() { return jpa.findAll().stream().map(this::toDomain).toList(); }
    @Override public long count() { return jpa.count(); }

    private IntegrationMessageJpaEntity toEntity(IntegrationMessage m) {
        var e = new IntegrationMessageJpaEntity();
        e.setId(UUID.fromString(m.getId())); e.setDirection(m.getDirection().name());
        e.setResourceType(m.getResourceType().name()); e.setExternalSystemId(m.getExternalSystemId());
        e.setCorrelationId(m.getCorrelationId()); e.setFhirPayload(m.getFhirPayload());
        e.setStatus(m.getStatus().name()); e.setErrorMessage(m.getErrorMessage());
        e.setInternalResourceId(m.getInternalResourceId());
        e.setCreatedAt(m.getCreatedAt()); e.setProcessedAt(m.getProcessedAt()); e.setUpdatedAt(m.getUpdatedAt());
        return e;
    }

    private IntegrationMessage toDomain(IntegrationMessageJpaEntity e) {
        return IntegrationMessage.reconstitute(e.getId().toString(),
                IntegrationDirection.valueOf(e.getDirection()),
                FhirResourceType.valueOf(e.getResourceType()),
                e.getExternalSystemId(), e.getCorrelationId(), e.getFhirPayload(),
                IntegrationStatus.valueOf(e.getStatus()), e.getErrorMessage(),
                e.getInternalResourceId(), e.getCreatedAt(), e.getProcessedAt(), e.getUpdatedAt());
    }
}
