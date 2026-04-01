-- Flyway migration: initial schema for patient-service

CREATE TABLE IF NOT EXISTS patients (
    id                UUID PRIMARY KEY,
    mrn               VARCHAR(20) NOT NULL UNIQUE,
    first_name        VARCHAR(100) NOT NULL,
    middle_name       VARCHAR(100),
    last_name         VARCHAR(100) NOT NULL,
    date_of_birth     DATE NOT NULL,
    gender            VARCHAR(20) NOT NULL,
    blood_type        VARCHAR(20),
    contact_info      JSONB NOT NULL DEFAULT '{}',
    emergency_contact JSONB,
    insurance_info    JSONB,
    allergies         JSONB NOT NULL DEFAULT '[]',
    active            BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_patients_mrn ON patients(mrn);
CREATE INDEX IF NOT EXISTS idx_patients_last_name ON patients(last_name);
CREATE INDEX IF NOT EXISTS idx_patients_active ON patients(active);
