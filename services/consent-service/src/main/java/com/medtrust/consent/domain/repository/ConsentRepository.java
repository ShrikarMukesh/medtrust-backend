package com.medtrust.consent.domain.repository;

import com.medtrust.consent.domain.model.Consent;
import com.medtrust.consent.domain.model.ConsentScope;
import java.util.List;
import java.util.Optional;

public interface ConsentRepository {
    Consent save(Consent consent);
    Optional<Consent> findById(String id);
    List<Consent> findByPatientId(String patientId);
    List<Consent> findByGrantedToUserId(String userId);
    List<Consent> findActiveByPatientIdAndGrantedToUserId(String patientId, String userId);
    Optional<Consent> findActiveByPatientIdAndGrantedToUserIdAndScope(
            String patientId, String userId, ConsentScope scope);
    List<Consent> findAll();
}
