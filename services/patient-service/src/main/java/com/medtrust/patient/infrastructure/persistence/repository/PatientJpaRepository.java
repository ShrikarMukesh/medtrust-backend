package com.medtrust.patient.infrastructure.persistence.repository;

import com.medtrust.patient.infrastructure.persistence.entity.PatientJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientJpaRepository extends JpaRepository<PatientJpaEntity, UUID> {
    Optional<PatientJpaEntity> findByMrn(String mrn);

    List<PatientJpaEntity> findByLastNameContainingIgnoreCase(String lastName);
}
