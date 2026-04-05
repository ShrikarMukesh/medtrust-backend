-- Flyway migration: immutable audit trail schema
-- HIPAA §164.530(j): 7+ year retention

CREATE TABLE IF NOT EXISTS audit_entries (
    id               UUID        PRIMARY KEY,
    event_type       VARCHAR(100) NOT NULL,
    category         VARCHAR(50)  NOT NULL,
    source_service   VARCHAR(100) NOT NULL,
    actor_id         VARCHAR(100),
    target_id        VARCHAR(100),
    target_type      VARCHAR(50),
    payload          JSONB,
    event_timestamp  TIMESTAMP    NOT NULL,
    received_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ── Indexes for audit search queries ──
CREATE INDEX IF NOT EXISTS idx_audit_event_type    ON audit_entries(event_type);
CREATE INDEX IF NOT EXISTS idx_audit_category      ON audit_entries(category);
CREATE INDEX IF NOT EXISTS idx_audit_source        ON audit_entries(source_service);
CREATE INDEX IF NOT EXISTS idx_audit_actor         ON audit_entries(actor_id);
CREATE INDEX IF NOT EXISTS idx_audit_target        ON audit_entries(target_id);
CREATE INDEX IF NOT EXISTS idx_audit_timestamp     ON audit_entries(event_timestamp);

-- GIN index for JSONB payload queries (e.g. searching by patientId within payload)
CREATE INDEX IF NOT EXISTS idx_audit_payload       ON audit_entries USING GIN (payload);

-- ── IMMUTABILITY: Prevent UPDATE and DELETE via trigger ──
CREATE OR REPLACE FUNCTION prevent_audit_mutation()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Audit entries are immutable. UPDATE and DELETE are not permitted.';
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER tr_audit_no_update
    BEFORE UPDATE ON audit_entries
    FOR EACH ROW EXECUTE FUNCTION prevent_audit_mutation();

CREATE OR REPLACE TRIGGER tr_audit_no_delete
    BEFORE DELETE ON audit_entries
    FOR EACH ROW EXECUTE FUNCTION prevent_audit_mutation();
