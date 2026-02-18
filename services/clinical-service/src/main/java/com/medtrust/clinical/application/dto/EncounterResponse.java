package com.medtrust.clinical.application.dto;

import java.util.List;

public record EncounterResponse(
        String id,
        String patientId,
        String status,
        String startDate,
        String endDate,
        List<ClinicalNoteResponse> clinicalNotes,
        List<DiagnosisResponse> diagnoses,
        String createdAt,
        String updatedAt) {
    public record ClinicalNoteResponse(
            String id,
            String content,
            String authorId,
            String noteType,
            String createdAt) {
    }

    public record DiagnosisResponse(
            String code,
            String description) {
    }
}
