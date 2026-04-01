package com.medtrust.patient.domain.model;

import com.medtrust.patient.domain.event.DomainEvent;
import com.medtrust.patient.domain.event.PatientContactUpdatedEvent;
import com.medtrust.patient.domain.event.PatientDeactivatedEvent;
import com.medtrust.patient.domain.event.PatientRegisteredEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Patient {

    private final String id;
    private final String mrn;
    private final String firstName;
    private final String middleName;
    private final String lastName;
    private final LocalDate dateOfBirth;
    private final Gender gender;
    private final BloodType bloodType;
    private ContactInfo contactInfo;
    private EmergencyContact emergencyContact;
    private InsuranceInfo insuranceInfo;
    private List<String> allergies;
    private boolean active;
    private final Instant createdAt;
    private Instant updatedAt;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Patient(String id, String mrn, String firstName, String middleName,
                    String lastName, LocalDate dateOfBirth, Gender gender,
                    BloodType bloodType, ContactInfo contactInfo,
                    EmergencyContact emergencyContact, InsuranceInfo insuranceInfo,
                    List<String> allergies, boolean active,
                    Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.mrn = mrn;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.bloodType = bloodType;
        this.contactInfo = contactInfo;
        this.emergencyContact = emergencyContact;
        this.insuranceInfo = insuranceInfo;
        this.allergies = allergies != null ? new ArrayList<>(allergies) : new ArrayList<>();
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Patient create(String mrn, String firstName, String middleName,
                                  String lastName, LocalDate dateOfBirth, Gender gender,
                                  BloodType bloodType, ContactInfo contactInfo,
                                  EmergencyContact emergencyContact, InsuranceInfo insuranceInfo,
                                  List<String> allergies) {
        var patient = new Patient(
                UUID.randomUUID().toString(),
                mrn,
                firstName,
                middleName,
                lastName,
                dateOfBirth,
                gender,
                bloodType,
                contactInfo,
                emergencyContact,
                insuranceInfo,
                allergies,
                true,
                Instant.now(),
                Instant.now());
        patient.addDomainEvent(new PatientRegisteredEvent(patient.id, mrn, firstName, lastName));
        return patient;
    }

    public static Patient reconstitute(String id, String mrn, String firstName, String middleName,
                                        String lastName, LocalDate dateOfBirth, Gender gender,
                                        BloodType bloodType, ContactInfo contactInfo,
                                        EmergencyContact emergencyContact, InsuranceInfo insuranceInfo,
                                        List<String> allergies, boolean active,
                                        Instant createdAt, Instant updatedAt) {
        return new Patient(id, mrn, firstName, middleName, lastName, dateOfBirth, gender,
                bloodType, contactInfo, emergencyContact, insuranceInfo, allergies, active,
                createdAt, updatedAt);
    }

    // ── Domain behaviour ──

    public void updateContactInfo(ContactInfo newContactInfo) {
        this.contactInfo = newContactInfo;
        this.updatedAt = Instant.now();
        addDomainEvent(new PatientContactUpdatedEvent(this.id));
    }

    public void updateEmergencyContact(EmergencyContact newEmergencyContact) {
        this.emergencyContact = newEmergencyContact;
        this.updatedAt = Instant.now();
    }

    public void updateInsurance(InsuranceInfo newInsuranceInfo) {
        this.insuranceInfo = newInsuranceInfo;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        if (!this.active) {
            throw new IllegalStateException("Patient is already deactivated");
        }
        this.active = false;
        this.updatedAt = Instant.now();
        addDomainEvent(new PatientDeactivatedEvent(this.id, this.mrn));
    }

    public void reactivate() {
        if (this.active) {
            throw new IllegalStateException("Patient is already active");
        }
        this.active = true;
        this.updatedAt = Instant.now();
    }

    // ── Domain events ──

    private void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public List<DomainEvent> pullDomainEvents() {
        var events = List.copyOf(this.domainEvents);
        this.domainEvents.clear();
        return events;
    }

    // ── Getters ──

    public String getId() {
        return id;
    }

    public String getMrn() {
        return mrn;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        if (middleName != null && !middleName.isBlank()) {
            return firstName + " " + middleName + " " + lastName;
        }
        return firstName + " " + lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public BloodType getBloodType() {
        return bloodType;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public EmergencyContact getEmergencyContact() {
        return emergencyContact;
    }

    public InsuranceInfo getInsuranceInfo() {
        return insuranceInfo;
    }

    public List<String> getAllergies() {
        return Collections.unmodifiableList(allergies);
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
