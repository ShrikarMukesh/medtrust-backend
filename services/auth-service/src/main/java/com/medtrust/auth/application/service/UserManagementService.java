package com.medtrust.auth.application.service;

import com.medtrust.auth.application.dto.ChangePasswordRequest;
import com.medtrust.auth.application.dto.UserResponse;
import com.medtrust.auth.domain.model.User;
import com.medtrust.auth.domain.repository.RefreshTokenRepository;
import com.medtrust.auth.domain.repository.UserRepository;
import com.medtrust.auth.infrastructure.kafka.AuthKafkaProducer;
import com.medtrust.auth.infrastructure.security.PasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserManagementService {

    private static final Logger log = LoggerFactory.getLogger(UserManagementService.class);
    private static final String AUTH_EVENTS_TOPIC = "auth-events";

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordService passwordService;
    private final AuthKafkaProducer kafkaProducer;

    public UserManagementService(UserRepository userRepository,
                                  RefreshTokenRepository refreshTokenRepository,
                                  PasswordService passwordService,
                                  AuthKafkaProducer kafkaProducer) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordService = passwordService;
        this.kafkaProducer = kafkaProducer;
    }

    @Transactional(readOnly = true)
    public UserResponse findById(String id) {
        User user = findByIdOrThrow(id);
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse changePassword(String userId, ChangePasswordRequest request) {
        User user = findByIdOrThrow(userId);

        if (!passwordService.verify(request.oldPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        String newHash = passwordService.hash(request.newPassword());
        user.changePassword(newHash);
        User saved = userRepository.save(user);

        // Revoke all refresh tokens on password change
        refreshTokenRepository.revokeAllByUserId(userId);

        publishDomainEvents(saved);
        return toResponse(saved);
    }

    public UserResponse deactivate(String userId) {
        User user = findByIdOrThrow(userId);
        user.deactivate();
        User saved = userRepository.save(user);

        // Revoke all refresh tokens
        refreshTokenRepository.revokeAllByUserId(userId);

        publishDomainEvents(saved);
        return toResponse(saved);
    }

    public UserResponse reactivate(String userId) {
        User user = findByIdOrThrow(userId);
        user.reactivate();
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    private User findByIdOrThrow(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + id + " not found"));
    }

    private void publishDomainEvents(User user) {
        user.pullDomainEvents().forEach(event -> {
            log.info("[UserManagement] Publishing domain event: {}", event.eventType());
            kafkaProducer.publish(AUTH_EVENTS_TOPIC, event);
        });
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                user.isActive(),
                user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : null,
                user.getCreatedAt().toString());
    }
}
