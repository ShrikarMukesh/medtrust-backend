-- Flyway migration: initial schema for consent-service

CREATE TABLE IF NOT EXISTS consents (
    id                  UUID        PRIMARY KEY,
    patient_id          UUID        NOT NULL,
    granted_to_user_id  UUID        NOT NULL,
    scope               VARCHAR(50) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    valid_from          DATE        NOT NULL,
    valid_until         DATE,
    reason              VARCHAR(500),
    revoked_at          TIMESTAMP,
    created_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- Composite index for the core verification query
CREATE INDEX IF NOT EXISTS idx_consents_verify
    ON consents(patient_id, granted_to_user_id, scope, status);

CREATE INDEX IF NOT EXISTS idx_consents_patient_id ON consents(patient_id);
CREATE INDEX IF NOT EXISTS idx_consents_granted_to ON consents(granted_to_user_id);
CREATE INDEX IF NOT EXISTS idx_consents_status ON consents(status);

-- Prevent duplicate active consents for same patient+user+scope
CREATE UNIQUE INDEX IF NOT EXISTS idx_consents_unique_active
    ON consents(patient_id, granted_to_user_id, scope) WHERE status = 'ACTIVE';
