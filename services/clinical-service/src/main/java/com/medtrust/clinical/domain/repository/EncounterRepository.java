package com.medtrust.clinical.domain.repository;

import com.medtrust.clinical.domain.model.Encounter;

import java.util.List;
import java.util.Optional;

public interface EncounterRepository {
    Encounter save(Encounter encounter);

    Optional<Encounter> findById(String id);

    List<Encounter> findByPatientId(String patientId);

    List<Encounter> findAll();
}
