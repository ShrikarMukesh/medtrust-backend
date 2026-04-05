-- Flyway migration: notification tracking schema

CREATE TABLE IF NOT EXISTS notifications (
    id                  UUID         PRIMARY KEY,
    recipient_id        VARCHAR(100) NOT NULL,
    recipient_contact   VARCHAR(255) NOT NULL,
    channel             VARCHAR(20)  NOT NULL,
    template_name       VARCHAR(100),
    subject             VARCHAR(255),
    body                TEXT         NOT NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    provider_message_id VARCHAR(255),
    failure_reason      VARCHAR(500),
    retry_count         INT          NOT NULL DEFAULT 0,
    source_event        VARCHAR(100),
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    sent_at             TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_notif_recipient ON notifications(recipient_id);
CREATE INDEX IF NOT EXISTS idx_notif_status ON notifications(status);
CREATE INDEX IF NOT EXISTS idx_notif_channel ON notifications(channel);
CREATE INDEX IF NOT EXISTS idx_notif_source ON notifications(source_event);
CREATE INDEX IF NOT EXISTS idx_notif_created ON notifications(created_at);
