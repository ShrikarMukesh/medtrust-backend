package com.medtrust.notification.application.port;

/**
 * Strategy interface for notification channel senders.
 * Each channel (WhatsApp, SMS, Email) implements this interface.
 */
public interface NotificationSender {

    /**
     * Send a notification. Returns the provider message ID.
     */
    String send(String recipientContact, String subject, String body);

    /**
     * Which channel does this sender handle?
     */
    String channel();
}
