package com.medtrust.consent.domain.model;

import com.medtrust.consent.domain.event.ConsentGrantedEvent;
import com.medtrust.consent.domain.event.ConsentRevokedEvent;
import com.medtrust.consent.domain.event.DomainEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Consent aggregate root.
 *
 * Represents a patient's explicit consent granting a specific user
 * (e.g. a doctor) access to a defined scope of their health data
 * within a time window.
 *
 * HIPAA §164.508 requires that consent be:
 * - Specific (scope)
 * - Time-bound (validFrom/validUntil)
 * - Revocable (revoke())
 * - Auditable (domain events)
 */
public class Consent {

    private final String id;
    private final String patientId;
    private final String grantedToUserId;
    private final ConsentScope scope;
    private ConsentStatus status;
    private final LocalDate validFrom;
    private final LocalDate validUntil;
    private final String reason;
    private Instant revokedAt;
    private final Instant createdAt;
    private Instant updatedAt;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Consent(String id, String patientId, String grantedToUserId,
                    ConsentScope scope, ConsentStatus status,
                    LocalDate validFrom, LocalDate validUntil, String reason,
                    Instant revokedAt, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.patientId = patientId;
        this.grantedToUserId = grantedToUserId;
        this.scope = scope;
        this.status = status;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.reason = reason;
        this.revokedAt = revokedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Consent grant(String patientId, String grantedToUserId,
                                 ConsentScope scope, LocalDate validFrom,
                                 LocalDate validUntil, String reason) {
        if (validUntil != null && validFrom != null && validUntil.isBefore(validFrom)) {
            throw new IllegalArgumentException("validUntil must be after validFrom");
        }

        var consent = new Consent(
                UUID.randomUUID().toString(),
                patientId,
                grantedToUserId,
                scope,
                ConsentStatus.ACTIVE,
                validFrom != null ? validFrom : LocalDate.now(),
                validUntil,
                reason,
                null,
                Instant.now(),
                Instant.now());

        consent.addDomainEvent(new ConsentGrantedEvent(
                consent.id, patientId, grantedToUserId, scope.name()));
        return consent;
    }

    public static Consent reconstitute(String id, String patientId, String grantedToUserId,
                                        ConsentScope scope, ConsentStatus status,
                                        LocalDate validFrom, LocalDate validUntil, String reason,
                                        Instant revokedAt, Instant createdAt, Instant updatedAt) {
        return new Consent(id, patientId, grantedToUserId, scope, status,
                validFrom, validUntil, reason, revokedAt, createdAt, updatedAt);
    }

    // ── Domain behaviour ──

    public void revoke() {
        if (this.status == ConsentStatus.REVOKED) {
            throw new IllegalStateException("Consent is already revoked");
        }
        this.status = ConsentStatus.REVOKED;
        this.revokedAt = Instant.now();
        this.updatedAt = Instant.now();
        addDomainEvent(new ConsentRevokedEvent(this.id, this.patientId, this.grantedToUserId));
    }

    public boolean isCurrentlyValid() {
        if (status == ConsentStatus.REVOKED) return false;
        LocalDate today = LocalDate.now();
        if (validFrom != null && today.isBefore(validFrom)) return false;
        if (validUntil != null && today.isAfter(validUntil)) return false;
        return true;
    }

    // ── Domain events ──

    private void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public List<DomainEvent> pullDomainEvents() {
        var events = List.copyOf(this.domainEvents);
        this.domainEvents.clear();
        return events;
    }

    // ── Getters ──

    public String getId() { return id; }
    public String getPatientId() { return patientId; }
    public String getGrantedToUserId() { return grantedToUserId; }
    public ConsentScope getScope() { return scope; }
    public ConsentStatus getStatus() { return status; }
    public LocalDate getValidFrom() { return validFrom; }
    public LocalDate getValidUntil() { return validUntil; }
    public String getReason() { return reason; }
    public Instant getRevokedAt() { return revokedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
