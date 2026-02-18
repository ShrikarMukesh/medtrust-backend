package com.medtrust.clinical.infrastructure.persistence.adapter;

import com.medtrust.clinical.domain.model.ContactInfo;
import com.medtrust.clinical.domain.model.Gender;
import com.medtrust.clinical.domain.model.Patient;
import com.medtrust.clinical.domain.repository.PatientRepository;
import com.medtrust.clinical.infrastructure.persistence.entity.ContactInfoData;
import com.medtrust.clinical.infrastructure.persistence.entity.PatientJpaEntity;
import com.medtrust.clinical.infrastructure.persistence.repository.PatientJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JpaPatientRepositoryAdapter implements PatientRepository {

    private final PatientJpaRepository jpaRepository;

    public JpaPatientRepositoryAdapter(PatientJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Patient save(Patient patient) {
        PatientJpaEntity entity = toEntity(patient);
        PatientJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Patient> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id))
                .map(this::toDomain);
    }

    @Override
    public Optional<Patient> findByMrn(String mrn) {
        return jpaRepository.findByMrn(mrn)
                .map(this::toDomain);
    }

    @Override
    public List<Patient> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    private PatientJpaEntity toEntity(Patient patient) {
        PatientJpaEntity entity = new PatientJpaEntity();
        entity.setId(UUID.fromString(patient.getId()));
        entity.setMrn(patient.getMrn());
        entity.setFirstName(patient.getFirstName());
        entity.setLastName(patient.getLastName());
        entity.setDateOfBirth(patient.getDateOfBirth());
        entity.setGender(patient.getGender().name());
        entity.setContactInfo(new ContactInfoData(
                patient.getContactInfo().phone(),
                patient.getContactInfo().email(),
                patient.getContactInfo().address()));
        entity.setCreatedAt(patient.getCreatedAt());
        entity.setUpdatedAt(patient.getUpdatedAt());
        return entity;
    }

    private Patient toDomain(PatientJpaEntity entity) {
        return Patient.reconstitute(
                entity.getId().toString(),
                entity.getMrn(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getDateOfBirth(),
                Gender.valueOf(entity.getGender()),
                new ContactInfo(
                        entity.getContactInfo().getPhone(),
                        entity.getContactInfo().getEmail(),
                        entity.getContactInfo().getAddress()),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
