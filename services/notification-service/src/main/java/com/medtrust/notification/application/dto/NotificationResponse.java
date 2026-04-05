package com.medtrust.notification.application.dto;

public record NotificationResponse(
        String id,
        String recipientId,
        String recipientContact,
        String channel,
        String templateName,
        String subject,
        String body,
        String status,
        String providerMessageId,
        String failureReason,
        int retryCount,
        String sourceEvent,
        String createdAt,
        String sentAt,
        String updatedAt) {
}
