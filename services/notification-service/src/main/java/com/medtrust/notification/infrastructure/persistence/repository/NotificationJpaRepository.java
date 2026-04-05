package com.medtrust.notification.infrastructure.persistence.repository;

import com.medtrust.notification.infrastructure.persistence.entity.NotificationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, UUID> {
    List<NotificationJpaEntity> findByRecipientId(String recipientId);
    List<NotificationJpaEntity> findByStatus(String status);
    List<NotificationJpaEntity> findByChannel(String channel);
}
