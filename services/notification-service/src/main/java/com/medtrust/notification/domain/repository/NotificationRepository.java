package com.medtrust.notification.domain.repository;

import com.medtrust.notification.domain.model.Notification;
import com.medtrust.notification.domain.model.NotificationChannel;
import com.medtrust.notification.domain.model.NotificationStatus;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository {
    Notification save(Notification notification);
    Optional<Notification> findById(String id);
    List<Notification> findByRecipientId(String recipientId);
    List<Notification> findByStatus(NotificationStatus status);
    List<Notification> findByChannel(NotificationChannel channel);
    List<Notification> findAll();
    long count();
}
