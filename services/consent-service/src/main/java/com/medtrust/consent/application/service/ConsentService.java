package com.medtrust.consent.application.service;

import com.medtrust.consent.application.dto.*;
import com.medtrust.consent.domain.model.Consent;
import com.medtrust.consent.domain.model.ConsentScope;
import com.medtrust.consent.domain.repository.ConsentRepository;
import com.medtrust.consent.infrastructure.kafka.ConsentKafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ConsentService {

    private static final Logger log = LoggerFactory.getLogger(ConsentService.class);
    private static final String CONSENT_EVENTS_TOPIC = "consent-events";

    private final ConsentRepository consentRepository;
    private final ConsentKafkaProducer kafkaProducer;

    public ConsentService(ConsentRepository consentRepository,
                          ConsentKafkaProducer kafkaProducer) {
        this.consentRepository = consentRepository;
        this.kafkaProducer = kafkaProducer;
    }

    public ConsentResponse grantConsent(GrantConsentRequest request) {
        ConsentScope scope = ConsentScope.valueOf(request.scope().toUpperCase());

        // Check if active consent already exists for same patient+user+scope
        consentRepository.findActiveByPatientIdAndGrantedToUserIdAndScope(
                request.patientId(), request.grantedToUserId(), scope)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Active consent already exists for this patient, user, and scope");
                });

        LocalDate validFrom = request.validFrom() != null
                ? LocalDate.parse(request.validFrom()) : LocalDate.now();
        LocalDate validUntil = request.validUntil() != null
                ? LocalDate.parse(request.validUntil()) : null;

        Consent consent = Consent.grant(
                request.patientId(),
                request.grantedToUserId(),
                scope,
                validFrom,
                validUntil,
                request.reason());

        Consent saved = consentRepository.save(consent);
        publishDomainEvents(saved);
        return toResponse(saved);
    }

    public ConsentResponse revokeConsent(String consentId) {
        Consent consent = findByIdOrThrow(consentId);
        consent.revoke();
        Consent saved = consentRepository.save(consent);
        publishDomainEvents(saved);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ConsentResponse findById(String id) {
        return toResponse(findByIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<ConsentResponse> findByPatientId(String patientId) {
        return consentRepository.findByPatientId(patientId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ConsentResponse> findByGrantedToUserId(String userId) {
        return consentRepository.findByGrantedToUserId(userId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ConsentResponse> findAll() {
        return consentRepository.findAll().stream()
                .map(this::toResponse).toList();
    }

    /**
     * Core verification endpoint — other services call this to check
     * if a user has consent to access a patient's data for a given scope.
     */
    @Transactional(readOnly = true)
    public ConsentVerificationResponse verifyConsent(
            String patientId, String userId, String scope) {
        ConsentScope consentScope = ConsentScope.valueOf(scope.toUpperCase());

        // Check for specific scope OR FULL_ACCESS
        var consent = consentRepository.findActiveByPatientIdAndGrantedToUserIdAndScope(
                patientId, userId, consentScope);

        if (consent.isPresent() && consent.get().isCurrentlyValid()) {
            return new ConsentVerificationResponse(
                    true, patientId, userId, scope,
                    consent.get().getId(),
                    consent.get().getValidUntil() != null
                            ? consent.get().getValidUntil().toString() : null);
        }

        // Fallback: check FULL_ACCESS
        if (consentScope != ConsentScope.FULL_ACCESS) {
            var fullAccess = consentRepository.findActiveByPatientIdAndGrantedToUserIdAndScope(
                    patientId, userId, ConsentScope.FULL_ACCESS);
            if (fullAccess.isPresent() && fullAccess.get().isCurrentlyValid()) {
                return new ConsentVerificationResponse(
                        true, patientId, userId, scope,
                        fullAccess.get().getId(),
                        fullAccess.get().getValidUntil() != null
                                ? fullAccess.get().getValidUntil().toString() : null);
            }
        }

        return new ConsentVerificationResponse(false, patientId, userId, scope, null, null);
    }

    private Consent findByIdOrThrow(String id) {
        return consentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Consent with ID " + id + " not found"));
    }

    private void publishDomainEvents(Consent consent) {
        consent.pullDomainEvents().forEach(event -> {
            log.info("[ConsentService] Publishing domain event: {}", event.eventType());
            kafkaProducer.publish(CONSENT_EVENTS_TOPIC, event);
        });
    }

    private ConsentResponse toResponse(Consent consent) {
        return new ConsentResponse(
                consent.getId(),
                consent.getPatientId(),
                consent.getGrantedToUserId(),
                consent.getScope().name(),
                consent.getStatus().name(),
                consent.getValidFrom() != null ? consent.getValidFrom().toString() : null,
                consent.getValidUntil() != null ? consent.getValidUntil().toString() : null,
                consent.getReason(),
                consent.isCurrentlyValid(),
                consent.getRevokedAt() != null ? consent.getRevokedAt().toString() : null,
                consent.getCreatedAt().toString(),
                consent.getUpdatedAt().toString());
    }
}
