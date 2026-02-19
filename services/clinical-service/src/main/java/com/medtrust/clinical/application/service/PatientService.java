package com.medtrust.clinical.application.service;

import com.medtrust.clinical.application.dto.PatientResponse;
import com.medtrust.clinical.application.dto.RegisterPatientRequest;
import com.medtrust.clinical.domain.model.ContactInfo;
import com.medtrust.clinical.domain.model.Gender;
import com.medtrust.clinical.domain.model.Patient;
import com.medtrust.clinical.domain.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public PatientResponse register(RegisterPatientRequest request) {
        patientRepository.findByMrn(request.mrn()).ifPresent(existing -> {
            throw new IllegalArgumentException("Patient with MRN " + request.mrn() + " already exists");
        });

        LocalDate dob = LocalDate.parse(request.dateOfBirth());

        Patient patient = Patient.create(
                request.mrn(),
                request.firstName(),
                request.lastName(),
                dob,
                Gender.valueOf(request.gender()),
                new ContactInfo(
                        request.contactInfo().phone(),
                        request.contactInfo().email(),
                        request.contactInfo().address()));

        Patient saved = patientRepository.save(patient);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PatientResponse findById(String id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient with ID " + id + " not found"));
        return toResponse(patient);
    }

    private PatientResponse toResponse(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getMrn(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getDateOfBirth().toString(),
                patient.getGender().name(),
                new PatientResponse.ContactInfoResponse(
                        patient.getContactInfo().phone(),
                        patient.getContactInfo().email(),
                        patient.getContactInfo().address()),
                patient.getCreatedAt().toString());
    }
}
