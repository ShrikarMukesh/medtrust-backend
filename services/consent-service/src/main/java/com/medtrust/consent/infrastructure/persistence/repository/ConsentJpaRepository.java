package com.medtrust.consent.infrastructure.persistence.repository;

import com.medtrust.consent.infrastructure.persistence.entity.ConsentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConsentJpaRepository extends JpaRepository<ConsentJpaEntity, UUID> {
    List<ConsentJpaEntity> findByPatientId(UUID patientId);
    List<ConsentJpaEntity> findByGrantedToUserId(UUID grantedToUserId);
    List<ConsentJpaEntity> findByPatientIdAndGrantedToUserIdAndStatus(
            UUID patientId, UUID grantedToUserId, String status);
    Optional<ConsentJpaEntity> findByPatientIdAndGrantedToUserIdAndScopeAndStatus(
            UUID patientId, UUID grantedToUserId, String scope, String status);
}
