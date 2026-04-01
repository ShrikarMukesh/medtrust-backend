package com.medtrust.auth.domain.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class RefreshToken {

    private final String id;
    private final String userId;
    private final String token;
    private final Instant expiresAt;
    private boolean revoked;
    private final Instant createdAt;

    private RefreshToken(String id, String userId, String token,
                         Instant expiresAt, boolean revoked, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
        this.createdAt = createdAt;
    }

    public static RefreshToken create(String userId, int ttlDays) {
        return new RefreshToken(
                UUID.randomUUID().toString(),
                userId,
                UUID.randomUUID().toString(),
                Instant.now().plus(ttlDays, ChronoUnit.DAYS),
                false,
                Instant.now());
    }

    public static RefreshToken reconstitute(String id, String userId, String token,
                                             Instant expiresAt, boolean revoked, Instant createdAt) {
        return new RefreshToken(id, userId, token, expiresAt, revoked, createdAt);
    }

    public void revoke() {
        this.revoked = true;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }

    // ── Getters ──

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getToken() { return token; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isRevoked() { return revoked; }
    public Instant getCreatedAt() { return createdAt; }
}
