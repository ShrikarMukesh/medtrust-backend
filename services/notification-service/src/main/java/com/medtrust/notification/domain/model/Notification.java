package com.medtrust.notification.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Notification aggregate — tracks each notification sent via any channel.
 */
public class Notification {

    private final String id;
    private final String recipientId;
    private final String recipientContact;
    private final NotificationChannel channel;
    private final String templateName;
    private final String subject;
    private final String body;
    private NotificationStatus status;
    private String providerMessageId;
    private String failureReason;
    private int retryCount;
    private final String sourceEvent;
    private final Instant createdAt;
    private Instant sentAt;
    private Instant updatedAt;

    private Notification(String id, String recipientId, String recipientContact,
                         NotificationChannel channel, String templateName,
                         String subject, String body, NotificationStatus status,
                         String providerMessageId, String failureReason,
                         int retryCount, String sourceEvent,
                         Instant createdAt, Instant sentAt, Instant updatedAt) {
        this.id = id;
        this.recipientId = recipientId;
        this.recipientContact = recipientContact;
        this.channel = channel;
        this.templateName = templateName;
        this.subject = subject;
        this.body = body;
        this.status = status;
        this.providerMessageId = providerMessageId;
        this.failureReason = failureReason;
        this.retryCount = retryCount;
        this.sourceEvent = sourceEvent;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
        this.updatedAt = updatedAt;
    }

    public static Notification create(String recipientId, String recipientContact,
                                       NotificationChannel channel, String templateName,
                                       String subject, String body, String sourceEvent) {
        return new Notification(
                UUID.randomUUID().toString(),
                recipientId, recipientContact,
                channel, templateName, subject, body,
                NotificationStatus.PENDING,
                null, null, 0, sourceEvent,
                Instant.now(), null, Instant.now());
    }

    public static Notification reconstitute(String id, String recipientId, String recipientContact,
                                             NotificationChannel channel, String templateName,
                                             String subject, String body, NotificationStatus status,
                                             String providerMessageId, String failureReason,
                                             int retryCount, String sourceEvent,
                                             Instant createdAt, Instant sentAt, Instant updatedAt) {
        return new Notification(id, recipientId, recipientContact, channel, templateName,
                subject, body, status, providerMessageId, failureReason,
                retryCount, sourceEvent, createdAt, sentAt, updatedAt);
    }

    // ── Domain behaviour ──

    public void markSent(String providerMessageId) {
        this.status = NotificationStatus.SENT;
        this.providerMessageId = providerMessageId;
        this.sentAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void markDelivered() {
        this.status = NotificationStatus.DELIVERED;
        this.updatedAt = Instant.now();
    }

    public void markFailed(String reason) {
        this.status = NotificationStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = Instant.now();
    }

    public void markRetrying() {
        this.status = NotificationStatus.RETRYING;
        this.retryCount++;
        this.updatedAt = Instant.now();
    }

    // ── Getters ──

    public String getId() { return id; }
    public String getRecipientId() { return recipientId; }
    public String getRecipientContact() { return recipientContact; }
    public NotificationChannel getChannel() { return channel; }
    public String getTemplateName() { return templateName; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public NotificationStatus getStatus() { return status; }
    public String getProviderMessageId() { return providerMessageId; }
    public String getFailureReason() { return failureReason; }
    public int getRetryCount() { return retryCount; }
    public String getSourceEvent() { return sourceEvent; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getSentAt() { return sentAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
