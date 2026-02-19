package com.medtrust.appointment.domain.repository;

import com.medtrust.appointment.domain.model.Appointment;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository {
    Appointment save(Appointment appointment);

    Optional<Appointment> findById(String id);

    List<Appointment> findByPatientId(String patientId);

    List<Appointment> findByProviderId(String providerId);

    List<Appointment> findByDateRange(Instant start, Instant end);

    List<Appointment> findAll();
}
