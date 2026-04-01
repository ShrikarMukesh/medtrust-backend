package com.medtrust.auth.infrastructure.persistence.adapter;

import com.medtrust.auth.domain.model.RefreshToken;
import com.medtrust.auth.domain.repository.RefreshTokenRepository;
import com.medtrust.auth.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import com.medtrust.auth.infrastructure.persistence.repository.RefreshTokenJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class JpaRefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;

    public JpaRefreshTokenRepositoryAdapter(RefreshTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        RefreshTokenJpaEntity entity = toEntity(token);
        RefreshTokenJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpaRepository.findByToken(token).map(this::toDomain);
    }

    @Override
    public void revokeAllByUserId(String userId) {
        jpaRepository.revokeAllByUserId(UUID.fromString(userId));
    }

    private RefreshTokenJpaEntity toEntity(RefreshToken token) {
        RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity();
        entity.setId(UUID.fromString(token.getId()));
        entity.setUserId(UUID.fromString(token.getUserId()));
        entity.setToken(token.getToken());
        entity.setExpiresAt(token.getExpiresAt());
        entity.setRevoked(token.isRevoked());
        entity.setCreatedAt(token.getCreatedAt());
        return entity;
    }

    private RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        return RefreshToken.reconstitute(
                entity.getId().toString(),
                entity.getUserId().toString(),
                entity.getToken(),
                entity.getExpiresAt(),
                entity.isRevoked(),
                entity.getCreatedAt());
    }
}
