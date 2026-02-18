package com.medtrust.clinical.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "clinical_notes")
public class ClinicalNoteJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "encounter_id", insertable = false, updatable = false, columnDefinition = "uuid")
    private UUID encounterId;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "author_id", nullable = false)
    private String authorId;

    @Column(name = "note_type", nullable = false)
    private String noteType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encounter_id", nullable = false)
    private EncounterJpaEntity encounter;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ClinicalNoteJpaEntity() {
    }

    // ── Getters and setters ──

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(UUID encounterId) {
        this.encounterId = encounterId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getNoteType() {
        return noteType;
    }

    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }

    public EncounterJpaEntity getEncounter() {
        return encounter;
    }

    public void setEncounter(EncounterJpaEntity encounter) {
        this.encounter = encounter;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
