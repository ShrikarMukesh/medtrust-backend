package com.medtrust.appointment.application.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record RescheduleAppointmentRequest(
        @NotNull Instant newStartTime,
        @NotNull Instant newEndTime) {
}
