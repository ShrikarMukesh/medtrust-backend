package com.medtrust.auth.application.service;

import com.medtrust.auth.application.dto.*;
import com.medtrust.auth.domain.model.RefreshToken;
import com.medtrust.auth.domain.model.Role;
import com.medtrust.auth.domain.model.User;
import com.medtrust.auth.domain.repository.RefreshTokenRepository;
import com.medtrust.auth.domain.repository.UserRepository;
import com.medtrust.auth.infrastructure.kafka.AuthKafkaProducer;
import com.medtrust.auth.infrastructure.security.JwtService;
import com.medtrust.auth.infrastructure.security.PasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final String AUTH_EVENTS_TOPIC = "auth-events";

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordService passwordService;
    private final AuthKafkaProducer kafkaProducer;

    @Value("${app.jwt.access-token-expiry-ms}")
    private long accessTokenExpiryMs;

    @Value("${app.jwt.refresh-token-expiry-days}")
    private int refreshTokenExpiryDays;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       JwtService jwtService,
                       PasswordService passwordService,
                       AuthKafkaProducer kafkaProducer) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.passwordService = passwordService;
        this.kafkaProducer = kafkaProducer;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email " + request.email() + " is already registered");
        }

        String passwordHash = passwordService.hash(request.password());
        Role role = Role.valueOf(request.role().toUpperCase());

        User user = User.create(request.email(), passwordHash,
                request.firstName(), request.lastName(), role);
        User saved = userRepository.save(user);
        publishDomainEvents(saved);

        return buildAuthResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!user.isActive()) {
            throw new IllegalStateException("Account is deactivated. Contact support.");
        }

        if (!passwordService.verify(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        user.recordLogin();
        User saved = userRepository.save(user);
        publishDomainEvents(saved);

        return buildAuthResponse(saved);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (!refreshToken.isValid()) {
            throw new IllegalArgumentException("Refresh token is expired or revoked");
        }

        // Revoke old refresh token (rotation)
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found for refresh token"));

        return buildAuthResponse(user);
    }

    public void logout(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);
        log.info("[AuthService] Refresh token revoked for user: {}", refreshToken.getUserId());
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);

        RefreshToken refreshToken = RefreshToken.create(user.getId(), refreshTokenExpiryDays);
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                "Bearer",
                accessTokenExpiryMs / 1000,
                new AuthResponse.UserInfo(
                        user.getId(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getRole().name()));
    }

    private void publishDomainEvents(User user) {
        user.pullDomainEvents().forEach(event -> {
            log.info("[AuthService] Publishing domain event: {}", event.eventType());
            kafkaProducer.publish(AUTH_EVENTS_TOPIC, event);
        });
    }
}
