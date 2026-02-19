-- Flyway migration: initial schema for clinical-service
-- Mirrors the existing TypeORM-generated schema

CREATE TABLE IF NOT EXISTS patients (
    id              UUID PRIMARY KEY,
    mrn             VARCHAR(255) NOT NULL UNIQUE,
    first_name      VARCHAR(255) NOT NULL,
    last_name       VARCHAR(255) NOT NULL,
    date_of_birth   DATE NOT NULL,
    gender          VARCHAR(50) NOT NULL,
    contact_info    JSONB NOT NULL DEFAULT '{}',
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS encounters (
    id              UUID PRIMARY KEY,
    patient_id      UUID NOT NULL,
    status          VARCHAR(50) NOT NULL DEFAULT 'PLANNED',
    start_date      TIMESTAMP NOT NULL,
    end_date        TIMESTAMP,
    diagnoses       JSONB NOT NULL DEFAULT '[]',
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS clinical_notes (
    id              UUID PRIMARY KEY,
    encounter_id    UUID NOT NULL,
    content         TEXT NOT NULL,
    author_id       VARCHAR(255) NOT NULL,
    note_type       VARCHAR(50) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_clinical_notes_encounter
        FOREIGN KEY (encounter_id) REFERENCES encounters(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_encounters_patient_id ON encounters(patient_id);
CREATE INDEX IF NOT EXISTS idx_clinical_notes_encounter_id ON clinical_notes(encounter_id);
CREATE INDEX IF NOT EXISTS idx_patients_mrn ON patients(mrn);
