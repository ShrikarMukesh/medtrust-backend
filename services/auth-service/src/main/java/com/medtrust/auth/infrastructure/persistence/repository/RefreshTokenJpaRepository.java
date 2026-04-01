package com.medtrust.auth.infrastructure.persistence.repository;

import com.medtrust.auth.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, UUID> {
    Optional<RefreshTokenJpaEntity> findByToken(String token);

    @Modifying
    @Query("UPDATE RefreshTokenJpaEntity rt SET rt.revoked = true WHERE rt.userId = :userId AND rt.revoked = false")
    void revokeAllByUserId(@Param("userId") UUID userId);
}
