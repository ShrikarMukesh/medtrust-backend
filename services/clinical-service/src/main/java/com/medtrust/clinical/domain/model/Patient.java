package com.medtrust.clinical.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Patient {

    private final String id;
    private final String mrn;
    private final String firstName;
    private final String lastName;
    private final Instant dateOfBirth;
    private final Gender gender;
    private final ContactInfo contactInfo;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Patient(String id, String mrn, String firstName, String lastName,
                    Instant dateOfBirth, Gender gender, ContactInfo contactInfo,
                    Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.mrn = mrn;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.contactInfo = contactInfo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Patient create(String mrn, String firstName, String lastName,
                                  Instant dateOfBirth, Gender gender, ContactInfo contactInfo) {
        return new Patient(
                UUID.randomUUID().toString(),
                mrn,
                firstName,
                lastName,
                dateOfBirth,
                gender,
                contactInfo,
                Instant.now(),
                Instant.now()
        );
    }

    public static Patient reconstitute(String id, String mrn, String firstName, String lastName,
                                        Instant dateOfBirth, Gender gender, ContactInfo contactInfo,
                                        Instant createdAt, Instant updatedAt) {
        return new Patient(id, mrn, firstName, lastName, dateOfBirth, gender,
                contactInfo, createdAt, updatedAt);
    }

    public String getId() {
        return id;
    }

    public String getMrn() {
        return mrn;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Instant getDateOfBirth() {
        return dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
