package com.medtrust.clinical.application.dto;

import jakarta.validation.constraints.NotBlank;

public record AddClinicalNoteRequest(
        @NotBlank String content,
        @NotBlank String authorId,
        @NotBlank String noteType) {
}
