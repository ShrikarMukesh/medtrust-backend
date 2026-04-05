package com.medtrust.consent.infrastructure.persistence.adapter;

import com.medtrust.consent.domain.model.Consent;
import com.medtrust.consent.domain.model.ConsentScope;
import com.medtrust.consent.domain.model.ConsentStatus;
import com.medtrust.consent.domain.repository.ConsentRepository;
import com.medtrust.consent.infrastructure.persistence.entity.ConsentJpaEntity;
import com.medtrust.consent.infrastructure.persistence.repository.ConsentJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JpaConsentRepositoryAdapter implements ConsentRepository {

    private final ConsentJpaRepository jpaRepository;

    public JpaConsentRepositoryAdapter(ConsentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Consent save(Consent consent) {
        return toDomain(jpaRepository.save(toEntity(consent)));
    }

    @Override
    public Optional<Consent> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id)).map(this::toDomain);
    }

    @Override
    public List<Consent> findByPatientId(String patientId) {
        return jpaRepository.findByPatientId(UUID.fromString(patientId))
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Consent> findByGrantedToUserId(String userId) {
        return jpaRepository.findByGrantedToUserId(UUID.fromString(userId))
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Consent> findActiveByPatientIdAndGrantedToUserId(String patientId, String userId) {
        return jpaRepository.findByPatientIdAndGrantedToUserIdAndStatus(
                UUID.fromString(patientId), UUID.fromString(userId), "ACTIVE")
                .stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Consent> findActiveByPatientIdAndGrantedToUserIdAndScope(
            String patientId, String userId, ConsentScope scope) {
        return jpaRepository.findByPatientIdAndGrantedToUserIdAndScopeAndStatus(
                UUID.fromString(patientId), UUID.fromString(userId),
                scope.name(), "ACTIVE")
                .map(this::toDomain);
    }

    @Override
    public List<Consent> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    private ConsentJpaEntity toEntity(Consent c) {
        ConsentJpaEntity e = new ConsentJpaEntity();
        e.setId(UUID.fromString(c.getId()));
        e.setPatientId(UUID.fromString(c.getPatientId()));
        e.setGrantedToUserId(UUID.fromString(c.getGrantedToUserId()));
        e.setScope(c.getScope().name());
        e.setStatus(c.getStatus().name());
        e.setValidFrom(c.getValidFrom());
        e.setValidUntil(c.getValidUntil());
        e.setReason(c.getReason());
        e.setRevokedAt(c.getRevokedAt());
        e.setCreatedAt(c.getCreatedAt());
        e.setUpdatedAt(c.getUpdatedAt());
        return e;
    }

    private Consent toDomain(ConsentJpaEntity e) {
        return Consent.reconstitute(
                e.getId().toString(),
                e.getPatientId().toString(),
                e.getGrantedToUserId().toString(),
                ConsentScope.valueOf(e.getScope()),
                ConsentStatus.valueOf(e.getStatus()),
                e.getValidFrom(),
                e.getValidUntil(),
                e.getReason(),
                e.getRevokedAt(),
                e.getCreatedAt(),
                e.getUpdatedAt());
    }
}
