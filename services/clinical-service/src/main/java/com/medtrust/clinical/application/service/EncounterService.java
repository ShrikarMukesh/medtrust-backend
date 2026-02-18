package com.medtrust.clinical.application.service;

import com.medtrust.clinical.application.dto.AddClinicalNoteRequest;
import com.medtrust.clinical.application.dto.ClinicalNoteResponseDTO;
import com.medtrust.clinical.application.dto.CreateEncounterRequest;
import com.medtrust.clinical.application.dto.EncounterResponse;
import com.medtrust.clinical.domain.model.ClinicalNote;
import com.medtrust.clinical.domain.model.Encounter;
import com.medtrust.clinical.domain.model.NoteType;
import com.medtrust.clinical.domain.repository.EncounterRepository;
import com.medtrust.clinical.domain.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EncounterService {

    private final EncounterRepository encounterRepository;
    private final PatientRepository patientRepository;

    public EncounterService(EncounterRepository encounterRepository,
            PatientRepository patientRepository) {
        this.encounterRepository = encounterRepository;
        this.patientRepository = patientRepository;
    }

    public EncounterResponse create(CreateEncounterRequest request) {
        patientRepository.findById(request.patientId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Patient with ID " + request.patientId() + " not found"));

        Encounter encounter = Encounter.create(request.patientId());
        Encounter saved = encounterRepository.save(encounter);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public EncounterResponse getById(String id) {
        Encounter encounter = encounterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Encounter with ID " + id + " not found"));
        return toResponse(encounter);
    }

    public ClinicalNoteResponseDTO addNote(String encounterId, AddClinicalNoteRequest request) {
        Encounter encounter = encounterRepository.findById(encounterId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Encounter with ID " + encounterId + " not found"));

        ClinicalNote note = encounter.addNote(
                request.content(),
                request.authorId(),
                NoteType.valueOf(request.noteType()));

        encounterRepository.save(encounter);

        return new ClinicalNoteResponseDTO(
                note.getId(),
                note.getEncounterId(),
                note.getContent(),
                note.getAuthorId(),
                note.getNoteType().name(),
                note.getCreatedAt().toString());
    }

    private EncounterResponse toResponse(Encounter encounter) {
        return new EncounterResponse(
                encounter.getId(),
                encounter.getPatientId(),
                encounter.getStatus().name(),
                encounter.getStartDate().toString(),
                encounter.getEndDate() != null ? encounter.getEndDate().toString() : null,
                encounter.getClinicalNotes().stream()
                        .map(n -> new EncounterResponse.ClinicalNoteResponse(
                                n.getId(),
                                n.getContent(),
                                n.getAuthorId(),
                                n.getNoteType().name(),
                                n.getCreatedAt().toString()))
                        .toList(),
                encounter.getDiagnoses().stream()
                        .map(d -> new EncounterResponse.DiagnosisResponse(
                                d.getCode(),
                                d.getDescription()))
                        .toList(),
                encounter.getCreatedAt().toString(),
                encounter.getUpdatedAt().toString());
    }
}
