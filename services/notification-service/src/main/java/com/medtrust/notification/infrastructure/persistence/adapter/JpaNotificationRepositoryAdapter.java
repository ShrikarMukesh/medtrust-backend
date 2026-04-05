package com.medtrust.notification.infrastructure.persistence.adapter;

import com.medtrust.notification.domain.model.Notification;
import com.medtrust.notification.domain.model.NotificationChannel;
import com.medtrust.notification.domain.model.NotificationStatus;
import com.medtrust.notification.domain.repository.NotificationRepository;
import com.medtrust.notification.infrastructure.persistence.entity.NotificationJpaEntity;
import com.medtrust.notification.infrastructure.persistence.repository.NotificationJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JpaNotificationRepositoryAdapter implements NotificationRepository {

    private final NotificationJpaRepository jpaRepo;

    public JpaNotificationRepositoryAdapter(NotificationJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override public Notification save(Notification n) { return toDomain(jpaRepo.save(toEntity(n))); }
    @Override public Optional<Notification> findById(String id) { return jpaRepo.findById(UUID.fromString(id)).map(this::toDomain); }
    @Override public List<Notification> findByRecipientId(String rid) { return jpaRepo.findByRecipientId(rid).stream().map(this::toDomain).toList(); }
    @Override public List<Notification> findByStatus(NotificationStatus s) { return jpaRepo.findByStatus(s.name()).stream().map(this::toDomain).toList(); }
    @Override public List<Notification> findByChannel(NotificationChannel c) { return jpaRepo.findByChannel(c.name()).stream().map(this::toDomain).toList(); }
    @Override public List<Notification> findAll() { return jpaRepo.findAll().stream().map(this::toDomain).toList(); }
    @Override public long count() { return jpaRepo.count(); }

    private NotificationJpaEntity toEntity(Notification n) {
        NotificationJpaEntity e = new NotificationJpaEntity();
        e.setId(UUID.fromString(n.getId()));
        e.setRecipientId(n.getRecipientId());
        e.setRecipientContact(n.getRecipientContact());
        e.setChannel(n.getChannel().name());
        e.setTemplateName(n.getTemplateName());
        e.setSubject(n.getSubject());
        e.setBody(n.getBody());
        e.setStatus(n.getStatus().name());
        e.setProviderMessageId(n.getProviderMessageId());
        e.setFailureReason(n.getFailureReason());
        e.setRetryCount(n.getRetryCount());
        e.setSourceEvent(n.getSourceEvent());
        e.setCreatedAt(n.getCreatedAt());
        e.setSentAt(n.getSentAt());
        e.setUpdatedAt(n.getUpdatedAt());
        return e;
    }

    private Notification toDomain(NotificationJpaEntity e) {
        return Notification.reconstitute(
                e.getId().toString(), e.getRecipientId(), e.getRecipientContact(),
                NotificationChannel.valueOf(e.getChannel()), e.getTemplateName(),
                e.getSubject(), e.getBody(), NotificationStatus.valueOf(e.getStatus()),
                e.getProviderMessageId(), e.getFailureReason(),
                e.getRetryCount(), e.getSourceEvent(),
                e.getCreatedAt(), e.getSentAt(), e.getUpdatedAt());
    }
}
