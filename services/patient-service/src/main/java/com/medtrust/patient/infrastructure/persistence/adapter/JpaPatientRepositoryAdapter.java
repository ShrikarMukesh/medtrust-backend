package com.medtrust.patient.infrastructure.persistence.adapter;

import com.medtrust.patient.domain.model.*;
import com.medtrust.patient.domain.repository.PatientRepository;
import com.medtrust.patient.infrastructure.persistence.entity.*;
import com.medtrust.patient.infrastructure.persistence.repository.PatientJpaRepository;
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

    @Override
    public List<Patient> findByLastName(String lastName) {
        return jpaRepository.findByLastNameContainingIgnoreCase(lastName).stream()
                .map(this::toDomain)
                .toList();
    }

    private PatientJpaEntity toEntity(Patient patient) {
        PatientJpaEntity entity = new PatientJpaEntity();
        entity.setId(UUID.fromString(patient.getId()));
        entity.setMrn(patient.getMrn());
        entity.setFirstName(patient.getFirstName());
        entity.setMiddleName(patient.getMiddleName());
        entity.setLastName(patient.getLastName());
        entity.setDateOfBirth(patient.getDateOfBirth());
        entity.setGender(patient.getGender().name());
        entity.setBloodType(patient.getBloodType() != null ? patient.getBloodType().name() : null);

        if (patient.getContactInfo() != null) {
            entity.setContactInfo(new ContactInfoData(
                    patient.getContactInfo().phone(),
                    patient.getContactInfo().email(),
                    patient.getContactInfo().address(),
                    patient.getContactInfo().city(),
                    patient.getContactInfo().state(),
                    patient.getContactInfo().zipCode()));
        }

        if (patient.getEmergencyContact() != null) {
            entity.setEmergencyContact(new EmergencyContactData(
                    patient.getEmergencyContact().name(),
                    patient.getEmergencyContact().relationship(),
                    patient.getEmergencyContact().phone()));
        }

        if (patient.getInsuranceInfo() != null) {
            entity.setInsuranceInfo(new InsuranceInfoData(
                    patient.getInsuranceInfo().provider(),
                    patient.getInsuranceInfo().policyNumber(),
                    patient.getInsuranceInfo().groupNumber(),
                    patient.getInsuranceInfo().expirationDate()));
        }

        entity.setAllergies(patient.getAllergies());
        entity.setActive(patient.isActive());
        entity.setCreatedAt(patient.getCreatedAt());
        entity.setUpdatedAt(patient.getUpdatedAt());
        return entity;
    }

    private Patient toDomain(PatientJpaEntity entity) {
        ContactInfo contactInfo = null;
        if (entity.getContactInfo() != null) {
            contactInfo = new ContactInfo(
                    entity.getContactInfo().getPhone(),
                    entity.getContactInfo().getEmail(),
                    entity.getContactInfo().getAddress(),
                    entity.getContactInfo().getCity(),
                    entity.getContactInfo().getState(),
                    entity.getContactInfo().getZipCode());
        }

        EmergencyContact emergencyContact = null;
        if (entity.getEmergencyContact() != null) {
            emergencyContact = new EmergencyContact(
                    entity.getEmergencyContact().getName(),
                    entity.getEmergencyContact().getRelationship(),
                    entity.getEmergencyContact().getPhone());
        }

        InsuranceInfo insuranceInfo = null;
        if (entity.getInsuranceInfo() != null) {
            insuranceInfo = new InsuranceInfo(
                    entity.getInsuranceInfo().getProvider(),
                    entity.getInsuranceInfo().getPolicyNumber(),
                    entity.getInsuranceInfo().getGroupNumber(),
                    entity.getInsuranceInfo().getExpirationDate());
        }

        BloodType bloodType = entity.getBloodType() != null
                ? BloodType.valueOf(entity.getBloodType())
                : null;

        return Patient.reconstitute(
                entity.getId().toString(),
                entity.getMrn(),
                entity.getFirstName(),
                entity.getMiddleName(),
                entity.getLastName(),
                entity.getDateOfBirth(),
                Gender.valueOf(entity.getGender()),
                bloodType,
                contactInfo,
                emergencyContact,
                insuranceInfo,
                entity.getAllergies(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
