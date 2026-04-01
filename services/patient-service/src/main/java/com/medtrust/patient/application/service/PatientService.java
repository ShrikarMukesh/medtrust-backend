package com.medtrust.patient.application.service;

import com.medtrust.patient.application.dto.*;
import com.medtrust.patient.domain.model.*;
import com.medtrust.patient.domain.repository.PatientRepository;
import com.medtrust.patient.infrastructure.kafka.PatientKafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class PatientService {

    private static final Logger log = LoggerFactory.getLogger(PatientService.class);
    private static final String PATIENT_EVENTS_TOPIC = "patient-events";

    private final PatientRepository patientRepository;
    private final PatientKafkaProducer kafkaProducer;

    public PatientService(PatientRepository patientRepository,
                          PatientKafkaProducer kafkaProducer) {
        this.patientRepository = patientRepository;
        this.kafkaProducer = kafkaProducer;
    }

    public PatientResponse register(RegisterPatientRequest request) {
        patientRepository.findByMrn(request.mrn()).ifPresent(existing -> {
            throw new IllegalArgumentException("Patient with MRN " + request.mrn() + " already exists");
        });

        LocalDate dob = LocalDate.parse(request.dateOfBirth());

        ContactInfo contactInfo = new ContactInfo(
                request.contactInfo().phone(),
                request.contactInfo().email(),
                request.contactInfo().address(),
                request.contactInfo().city(),
                request.contactInfo().state(),
                request.contactInfo().zipCode());

        EmergencyContact emergencyContact = request.emergencyContact() != null
                ? new EmergencyContact(
                        request.emergencyContact().name(),
                        request.emergencyContact().relationship(),
                        request.emergencyContact().phone())
                : null;

        InsuranceInfo insuranceInfo = request.insuranceInfo() != null
                ? new InsuranceInfo(
                        request.insuranceInfo().provider(),
                        request.insuranceInfo().policyNumber(),
                        request.insuranceInfo().groupNumber(),
                        request.insuranceInfo().expirationDate())
                : null;

        BloodType bloodType = request.bloodType() != null
                ? BloodType.valueOf(request.bloodType())
                : BloodType.UNKNOWN;

        Patient patient = Patient.create(
                request.mrn(),
                request.firstName(),
                request.middleName(),
                request.lastName(),
                dob,
                Gender.valueOf(request.gender()),
                bloodType,
                contactInfo,
                emergencyContact,
                insuranceInfo,
                request.allergies());

        Patient saved = patientRepository.save(patient);
        publishDomainEvents(saved);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PatientResponse findById(String id) {
        Patient patient = findByIdOrThrow(id);
        return toResponse(patient);
    }

    @Transactional(readOnly = true)
    public PatientResponse findByMrn(String mrn) {
        Patient patient = patientRepository.findByMrn(mrn)
                .orElseThrow(() -> new IllegalArgumentException("Patient with MRN " + mrn + " not found"));
        return toResponse(patient);
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> findAll() {
        return patientRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> searchByLastName(String lastName) {
        return patientRepository.findByLastName(lastName).stream()
                .map(this::toResponse)
                .toList();
    }

    public PatientResponse updateContactInfo(String id, UpdateContactInfoRequest request) {
        Patient patient = findByIdOrThrow(id);
        patient.updateContactInfo(new ContactInfo(
                request.phone(),
                request.email(),
                request.address(),
                request.city(),
                request.state(),
                request.zipCode()));

        Patient saved = patientRepository.save(patient);
        publishDomainEvents(saved);
        return toResponse(saved);
    }

    public PatientResponse updateInsurance(String id, UpdateInsuranceRequest request) {
        Patient patient = findByIdOrThrow(id);
        patient.updateInsurance(new InsuranceInfo(
                request.provider(),
                request.policyNumber(),
                request.groupNumber(),
                request.expirationDate()));

        Patient saved = patientRepository.save(patient);
        publishDomainEvents(saved);
        return toResponse(saved);
    }

    public PatientResponse deactivate(String id) {
        Patient patient = findByIdOrThrow(id);
        patient.deactivate();
        Patient saved = patientRepository.save(patient);
        publishDomainEvents(saved);
        return toResponse(saved);
    }

    private Patient findByIdOrThrow(String id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient with ID " + id + " not found"));
    }

    private void publishDomainEvents(Patient patient) {
        patient.pullDomainEvents().forEach(event -> {
            log.info("[PatientService] Publishing domain event: {}", event.eventType());
            kafkaProducer.publish(PATIENT_EVENTS_TOPIC, event);
        });
    }

    private PatientResponse toResponse(Patient patient) {
        PatientResponse.ContactInfoResponse contactResp = null;
        if (patient.getContactInfo() != null) {
            contactResp = new PatientResponse.ContactInfoResponse(
                    patient.getContactInfo().phone(),
                    patient.getContactInfo().email(),
                    patient.getContactInfo().address(),
                    patient.getContactInfo().city(),
                    patient.getContactInfo().state(),
                    patient.getContactInfo().zipCode());
        }

        PatientResponse.EmergencyContactResponse emergencyResp = null;
        if (patient.getEmergencyContact() != null) {
            emergencyResp = new PatientResponse.EmergencyContactResponse(
                    patient.getEmergencyContact().name(),
                    patient.getEmergencyContact().relationship(),
                    patient.getEmergencyContact().phone());
        }

        PatientResponse.InsuranceInfoResponse insuranceResp = null;
        if (patient.getInsuranceInfo() != null) {
            insuranceResp = new PatientResponse.InsuranceInfoResponse(
                    patient.getInsuranceInfo().provider(),
                    patient.getInsuranceInfo().policyNumber(),
                    patient.getInsuranceInfo().groupNumber(),
                    patient.getInsuranceInfo().expirationDate());
        }

        return new PatientResponse(
                patient.getId(),
                patient.getMrn(),
                patient.getFirstName(),
                patient.getMiddleName(),
                patient.getLastName(),
                patient.getFullName(),
                patient.getDateOfBirth().toString(),
                patient.getGender().name(),
                patient.getBloodType() != null ? patient.getBloodType().name() : null,
                contactResp,
                emergencyResp,
                insuranceResp,
                patient.getAllergies(),
                patient.isActive(),
                patient.getCreatedAt().toString(),
                patient.getUpdatedAt().toString());
    }
}
