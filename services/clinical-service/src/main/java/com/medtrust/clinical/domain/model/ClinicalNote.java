package com.medtrust.clinical.domain.model;

import java.time.Instant;
import java.util.UUID;

public class ClinicalNote {

    private final String id;
    private final String encounterId;
    private final String content;
    private final String authorId;
    private final NoteType noteType;
    private final Instant createdAt;

    private ClinicalNote(String id, String encounterId, String content,
            String authorId, NoteType noteType, Instant createdAt) {
        this.id = id;
        this.encounterId = encounterId;
        this.content = content;
        this.authorId = authorId;
        this.noteType = noteType;
        this.createdAt = createdAt;
    }

    public static ClinicalNote create(String encounterId, String content,
            String authorId, NoteType noteType) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Clinical note content cannot be empty");
        }
        return new ClinicalNote(
                UUID.randomUUID().toString(),
                encounterId,
                content,
                authorId,
                noteType,
                Instant.now());
    }

    public static ClinicalNote reconstitute(String id, String encounterId, String content,
            String authorId, NoteType noteType, Instant createdAt) {
        return new ClinicalNote(id, encounterId, content, authorId, noteType, createdAt);
    }

    public String getId() {
        return id;
    }

    public String getEncounterId() {
        return encounterId;
    }

    public String getContent() {
        return content;
    }

    public String getAuthorId() {
        return authorId;
    }

    public NoteType getNoteType() {
        return noteType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
