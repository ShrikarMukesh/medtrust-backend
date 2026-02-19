-- Flyway migration: initial schema for appointment-service

CREATE TABLE IF NOT EXISTS appointments (
    id              UUID PRIMARY KEY,
    patient_id      UUID NOT NULL,
    provider_id     UUID NOT NULL,
    start_time      TIMESTAMP NOT NULL,
    end_time        TIMESTAMP NOT NULL,
    status          VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    type            VARCHAR(50) NOT NULL,
    reason          TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_appointments_patient_id ON appointments(patient_id);
CREATE INDEX IF NOT EXISTS idx_appointments_provider_id ON appointments(provider_id);
CREATE INDEX IF NOT EXISTS idx_appointments_start_time ON appointments(start_time);
