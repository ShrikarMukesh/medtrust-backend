package com.medtrust.audit.domain.model;

/**
 * Categorizes audit events for filtering and retention policies.
 */
public enum AuditCategory {
    AUTHENTICATION,    // user.registered, user.logged_in, user.password_changed
    AUTHORIZATION,     // access denied, role changes
    CONSENT,           // consent.granted, consent.revoked
    PHI_ACCESS,        // patient data viewed/modified
    CLINICAL,          // encounter.created, note.added, diagnosis
    APPOINTMENT,       // appointment.scheduled, cancelled, rescheduled
    PATIENT,           // patient.registered, contact_updated, deactivated
    SYSTEM             // fallback for unrecognized events
}
