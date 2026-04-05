package com.medtrust.notification.application.dto;

import jakarta.validation.constraints.NotBlank;

public record SendNotificationRequest(
        @NotBlank String recipientId,
        @NotBlank String recipientContact,
        @NotBlank String channel,
        String templateName,
        String subject,
        @NotBlank String body) {
}
