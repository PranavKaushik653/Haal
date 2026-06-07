CREATE TABLE alert_logs (
                            id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                            user_id         UUID        NOT NULL REFERENCES users(id),
                            alert_type      VARCHAR(20) NOT NULL,   -- 'MISSED_CHECKIN' | 'SOS'
                            sent_to_name    TEXT        NOT NULL,   -- encrypted EC name at time of alert
                            sent_to_contact TEXT        NOT NULL,   -- encrypted EC phone/email
                            sent_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                            delivery_status VARCHAR(20) NOT NULL DEFAULT 'SENT',
                            notes           TEXT
);

CREATE INDEX idx_alert_logs_user
    ON alert_logs(user_id, sent_at DESC);