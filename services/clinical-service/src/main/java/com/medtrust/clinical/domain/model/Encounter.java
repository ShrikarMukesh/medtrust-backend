package com.medtrust.clinical.domain.model;

import com.medtrust.clinical.domain.event.DomainEvent;
import com.medtrust.clinical.domain.event.EncounterCreatedEvent;
import com.medtrust.clinical.domain.event.PatientAdmittedEvent;
import com.medtrust.clinical.domain.event.PatientDischargedEvent;
import com.medtrust.clinical.domain.model.valueobject.Diagnosis;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Encounter {

    private final String id;
    private final String patientId;
    private final List<ClinicalNote> clinicalNotes;
    private final List<Diagnosis> diagnoses;
    private EncounterStatus status;
    private final Instant startDate;
    private Instant endDate;
    private final Instant createdAt;
    private Instant updatedAt;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Encounter(String id, String patientId, List<ClinicalNote> clinicalNotes,
            List<Diagnosis> diagnoses, EncounterStatus status,
            Instant startDate, Instant endDate, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.patientId = patientId;
        this.clinicalNotes = new ArrayList<>(clinicalNotes);
        this.diagnoses = new ArrayList<>(diagnoses);
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Encounter create(String patientId) {
        var encounter = new Encounter(
                UUID.randomUUID().toString(),
                patientId,
                new ArrayList<>(),
                new ArrayList<>(),
                EncounterStatus.PLANNED,
                Instant.now(),
                null,
                Instant.now(),
                Instant.now());
        encounter.addDomainEvent(new EncounterCreatedEvent(encounter.id, patientId));
        return encounter;
    }

    public static Encounter reconstitute(String id, String patientId,
            List<ClinicalNote> clinicalNotes,
            List<Diagnosis> diagnoses,
            EncounterStatus status,
            Instant startDate, Instant endDate,
            Instant createdAt, Instant updatedAt) {
        return new Encounter(id, patientId, clinicalNotes, diagnoses,
                status, startDate, endDate, createdAt, updatedAt);
    }

    // ── Domain behaviour ──

    public void admit() {
        if (this.status != EncounterStatus.PLANNED) {
            throw new IllegalStateException("Only planned encounters can be admitted");
        }
        this.status = EncounterStatus.IN_PROGRESS;
        this.updatedAt = Instant.now();
        addDomainEvent(new PatientAdmittedEvent(this.id, this.patientId));
    }

    public ClinicalNote addNote(String content, String authorId, NoteType noteType) {
        if (this.status == EncounterStatus.COMPLETED || this.status == EncounterStatus.CANCELLED) {
            throw new IllegalStateException("Cannot add notes to a completed or cancelled encounter");
        }
        var note = ClinicalNote.create(this.id, content, authorId, noteType);
        this.clinicalNotes.add(note);
        this.updatedAt = Instant.now();
        return note;
    }

    public void addDiagnosis(Diagnosis diagnosis) {
        if (this.status == EncounterStatus.COMPLETED || this.status == EncounterStatus.CANCELLED) {
            throw new IllegalStateException("Cannot add diagnoses to a completed or cancelled encounter");
        }
        boolean exists = this.diagnoses.stream().anyMatch(d -> d.equals(diagnosis));
        if (exists) {
            throw new IllegalArgumentException(
                    "Diagnosis " + diagnosis.getCode() + " already exists on this encounter");
        }
        this.diagnoses.add(diagnosis);
        this.updatedAt = Instant.now();
    }

    public void discharge() {
        if (this.status != EncounterStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only in-progress encounters can be discharged");
        }
        this.status = EncounterStatus.COMPLETED;
        this.endDate = Instant.now();
        this.updatedAt = Instant.now();
        addDomainEvent(new PatientDischargedEvent(this.id, this.patientId, this.endDate));
    }

    public void cancel() {
        if (this.status == EncounterStatus.COMPLETED) {
            throw new IllegalStateException("Completed encounters cannot be cancelled");
        }
        this.status = EncounterStatus.CANCELLED;
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

    public String getPatientId() {
        return patientId;
    }

    public List<ClinicalNote> getClinicalNotes() {
        return Collections.unmodifiableList(clinicalNotes);
    }

    public List<Diagnosis> getDiagnoses() {
        return Collections.unmodifiableList(diagnoses);
    }

    public EncounterStatus getStatus() {
        return status;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
