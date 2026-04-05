package com.medtrust.consent.domain.model;

/**
 * Defines the specific data scopes a patient can grant access to.
 * Each scope maps to a bounded context in the MedTrust platform.
 */
public enum ConsentScope {
    VIEW_DEMOGRAPHICS,       // patient-service data
    VIEW_CLINICAL_RECORDS,   // clinical-service encounters + notes
    VIEW_APPOINTMENTS,       // appointment-service data
    VIEW_PRESCRIPTIONS,      // future prescription data
    VIEW_LAB_RESULTS,        // future lab results
    FULL_ACCESS              // all patient data
}
