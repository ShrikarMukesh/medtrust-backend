package com.medtrust.clinical.application.dto;

public record ClinicalNoteResponseDTO(
        String id,
        String encounterId,
        String content,
        String authorId,
        String noteType,
        String createdAt) {
}
