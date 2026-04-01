package com.medtrust.patient.domain.repository;

import com.medtrust.patient.domain.model.Patient;

import java.util.List;
import java.util.Optional;

public interface PatientRepository {
    Patient save(Patient patient);

    Optional<Patient> findById(String id);

    Optional<Patient> findByMrn(String mrn);

    List<Patient> findAll();

    List<Patient> findByLastName(String lastName);
}
