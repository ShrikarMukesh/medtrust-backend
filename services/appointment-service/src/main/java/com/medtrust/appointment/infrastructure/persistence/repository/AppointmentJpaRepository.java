package com.medtrust.appointment.infrastructure.persistence.repository;

import com.medtrust.appointment.infrastructure.persistence.entity.AppointmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentJpaRepository extends JpaRepository<AppointmentJpaEntity, UUID> {
    List<AppointmentJpaEntity> findByPatientId(UUID patientId);

    List<AppointmentJpaEntity> findByProviderId(UUID providerId);

    List<AppointmentJpaEntity> findByStartTimeBetween(Instant start, Instant end);
}
