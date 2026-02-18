package com.medtrust.clinical.domain.repository;

import com.medtrust.clinical.domain.model.Patient;

import java.util.List;
import java.util.Optional;

public interface PatientRepository {
    Patient save(Patient patient);

    Optional<Patient> findById(String id);

    Optional<Patient> findByMrn(String mrn);

    List<Patient> findAll();
}
