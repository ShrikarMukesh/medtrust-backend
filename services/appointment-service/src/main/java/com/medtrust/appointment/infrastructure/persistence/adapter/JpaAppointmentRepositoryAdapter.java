package com.medtrust.appointment.infrastructure.persistence.adapter;

import com.medtrust.appointment.domain.model.Appointment;
import com.medtrust.appointment.domain.model.AppointmentStatus;
import com.medtrust.appointment.domain.model.AppointmentType;
import com.medtrust.appointment.domain.repository.AppointmentRepository;
import com.medtrust.appointment.infrastructure.persistence.entity.AppointmentJpaEntity;
import com.medtrust.appointment.infrastructure.persistence.repository.AppointmentJpaRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JpaAppointmentRepositoryAdapter implements AppointmentRepository {

    private final AppointmentJpaRepository jpaRepository;

    public JpaAppointmentRepositoryAdapter(AppointmentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Appointment save(Appointment appointment) {
        AppointmentJpaEntity entity = toEntity(appointment);
        AppointmentJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Appointment> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id))
                .map(this::toDomain);
    }

    @Override
    public List<Appointment> findByPatientId(String patientId) {
        return jpaRepository.findByPatientId(UUID.fromString(patientId)).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Appointment> findByProviderId(String providerId) {
        return jpaRepository.findByProviderId(UUID.fromString(providerId)).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Appointment> findByDateRange(Instant start, Instant end) {
        return jpaRepository.findByStartTimeBetween(start, end).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Appointment> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    private AppointmentJpaEntity toEntity(Appointment appointment) {
        AppointmentJpaEntity entity = new AppointmentJpaEntity();
        entity.setId(UUID.fromString(appointment.getId()));
        entity.setPatientId(UUID.fromString(appointment.getPatientId()));
        entity.setProviderId(UUID.fromString(appointment.getProviderId()));
        entity.setStartTime(appointment.getStartTime());
        entity.setEndTime(appointment.getEndTime());
        entity.setStatus(appointment.getStatus().name());
        entity.setType(appointment.getType().name());
        entity.setReason(appointment.getReason());
        entity.setCreatedAt(appointment.getCreatedAt());
        entity.setUpdatedAt(appointment.getUpdatedAt());
        return entity;
    }

    private Appointment toDomain(AppointmentJpaEntity entity) {
        return Appointment.reconstitute(
                entity.getId().toString(),
                entity.getPatientId().toString(),
                entity.getProviderId().toString(),
                entity.getStartTime(),
                entity.getEndTime(),
                AppointmentStatus.valueOf(entity.getStatus()),
                AppointmentType.valueOf(entity.getType()),
                entity.getReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
