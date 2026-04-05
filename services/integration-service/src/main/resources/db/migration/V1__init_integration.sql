-- Flyway migration: FHIR integration message tracking

CREATE TABLE IF NOT EXISTS integration_messages (
    id                   UUID         PRIMARY KEY,
    direction            VARCHAR(20)  NOT NULL,
    resource_type        VARCHAR(50)  NOT NULL,
    external_system_id   VARCHAR(100) NOT NULL,
    correlation_id       VARCHAR(100) NOT NULL UNIQUE,
    fhir_payload         TEXT,
    status               VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    error_message        VARCHAR(500),
    internal_resource_id VARCHAR(100),
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    processed_at         TIMESTAMP,
    updated_at           TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_integ_direction  ON integration_messages(direction);
CREATE INDEX IF NOT EXISTS idx_integ_status     ON integration_messages(status);
CREATE INDEX IF NOT EXISTS idx_integ_ext_system ON integration_messages(external_system_id);
CREATE INDEX IF NOT EXISTS idx_integ_corr_id    ON integration_messages(correlation_id);
CREATE INDEX IF NOT EXISTS idx_integ_resource   ON integration_messages(resource_type);
