package com.medtrust.clinical.infrastructure.persistence.adapter;

import com.medtrust.clinical.domain.model.ClinicalNote;
import com.medtrust.clinical.domain.model.Encounter;
import com.medtrust.clinical.domain.model.EncounterStatus;
import com.medtrust.clinical.domain.model.NoteType;
import com.medtrust.clinical.domain.model.valueobject.Diagnosis;
import com.medtrust.clinical.domain.repository.EncounterRepository;
import com.medtrust.clinical.infrastructure.persistence.entity.ClinicalNoteJpaEntity;
import com.medtrust.clinical.infrastructure.persistence.entity.DiagnosisData;
import com.medtrust.clinical.infrastructure.persistence.entity.EncounterJpaEntity;
import com.medtrust.clinical.infrastructure.persistence.repository.EncounterJpaRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JpaEncounterRepositoryAdapter implements EncounterRepository {

    private final EncounterJpaRepository jpaRepository;

    public JpaEncounterRepositoryAdapter(EncounterJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Encounter save(Encounter encounter) {
        EncounterJpaEntity entity = toEntity(encounter);
        EncounterJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Encounter> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id))
                .map(this::toDomain);
    }

    @Override
    public List<Encounter> findByPatientId(String patientId) {
        return jpaRepository.findByPatientId(UUID.fromString(patientId)).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Encounter> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    private EncounterJpaEntity toEntity(Encounter encounter) {
        EncounterJpaEntity entity = new EncounterJpaEntity();
        entity.setId(UUID.fromString(encounter.getId()));
        entity.setPatientId(UUID.fromString(encounter.getPatientId()));
        entity.setStatus(encounter.getStatus().name());
        entity.setStartDate(encounter.getStartDate());
        entity.setEndDate(encounter.getEndDate());
        entity.setDiagnoses(encounter.getDiagnoses().stream()
                .map(d -> new DiagnosisData(d.getCode(), d.getDescription()))
                .toList());
        entity.setCreatedAt(encounter.getCreatedAt());
        entity.setUpdatedAt(encounter.getUpdatedAt());

        List<ClinicalNoteJpaEntity> noteEntities = new ArrayList<>();
        for (ClinicalNote note : encounter.getClinicalNotes()) {
            ClinicalNoteJpaEntity noteEntity = new ClinicalNoteJpaEntity();
            noteEntity.setId(UUID.fromString(note.getId()));
            noteEntity.setContent(note.getContent());
            noteEntity.setAuthorId(note.getAuthorId());
            noteEntity.setNoteType(note.getNoteType().name());
            noteEntity.setCreatedAt(note.getCreatedAt());
            noteEntity.setEncounter(entity);
            noteEntities.add(noteEntity);
        }
        entity.setClinicalNotes(noteEntities);

        return entity;
    }

    private Encounter toDomain(EncounterJpaEntity entity) {
        List<ClinicalNote> clinicalNotes = (entity.getClinicalNotes() != null)
                ? entity.getClinicalNotes().stream()
                        .map(n -> ClinicalNote.reconstitute(
                                n.getId().toString(),
                                entity.getId().toString(),
                                n.getContent(),
                                n.getAuthorId(),
                                NoteType.valueOf(n.getNoteType()),
                                n.getCreatedAt()))
                        .toList()
                : List.of();

        List<Diagnosis> diagnoses = (entity.getDiagnoses() != null)
                ? entity.getDiagnoses().stream()
                        .map(d -> Diagnosis.create(d.getCode(), d.getDescription()))
                        .toList()
                : List.of();

        return Encounter.reconstitute(
                entity.getId().toString(),
                entity.getPatientId().toString(),
                clinicalNotes,
                diagnoses,
                EncounterStatus.valueOf(entity.getStatus()),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
