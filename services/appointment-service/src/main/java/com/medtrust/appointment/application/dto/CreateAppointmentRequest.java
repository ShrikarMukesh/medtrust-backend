package com.medtrust.appointment.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record CreateAppointmentRequest(
        @NotBlank String patientId,
        @NotBlank String providerId,
        @NotNull Instant startTime,
        @NotNull Instant endTime,
        @NotBlank String type,
        String reason) {
}
