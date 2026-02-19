package com.medtrust.appointment.application.dto;

public record AppointmentResponse(
        String id,
        String patientId,
        String providerId,
        String startTime,
        String endTime,
        String status,
        String type,
        String reason,
        String createdAt,
        String updatedAt) {
}
