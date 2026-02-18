package com.medtrust.clinical.infrastructure.persistence.repository;

import com.medtrust.clinical.infrastructure.persistence.entity.EncounterJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EncounterJpaRepository extends JpaRepository<EncounterJpaEntity, UUID> {
    List<EncounterJpaEntity> findByPatientId(UUID patientId);
}
