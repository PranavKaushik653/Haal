-- V1__init_schema.sql
-- Foundation schema for Haal — Auth + Safety domains

-- Extensions
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS pgcrypto;   -- for gen_random_uuid() + encryption

-- =============================================
-- AUTH DOMAIN
-- =============================================
CREATE TABLE users (
                       id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                       anonymous_alias     VARCHAR(50) NOT NULL UNIQUE,
                       phone_hash          VARCHAR(255) NOT NULL UNIQUE,  -- BCrypt, login lookup
                       phone_encrypted     TEXT        NOT NULL,           -- AES, for SOS dispatch
                       ec_name_encrypted   TEXT        NOT NULL,           -- emergency contact name
                       ec_phone_encrypted  TEXT        NOT NULL,           -- emergency contact phone
                       ec_email_encrypted  TEXT,                           -- optional email alert
                       role                VARCHAR(20) NOT NULL DEFAULT 'USER',
                       is_active           BOOLEAN     NOT NULL DEFAULT TRUE,
                       created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                       updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =============================================
-- SAFETY DOMAIN
-- =============================================
CREATE TABLE check_ins (
                           id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                           user_id         UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                           checked_in_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                           location        GEOGRAPHY(POINT, 4326),   -- nullable, location optional
                           ip_address      VARCHAR(45),              -- for audit trail
                           alert_sent      BOOLEAN     NOT NULL DEFAULT FALSE,
                           alert_sent_at   TIMESTAMPTZ,
                           created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE sos_events (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            triggered_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                            location GEOGRAPHY(POINT, 4326),
                            resolved BOOLEAN NOT NULL DEFAULT FALSE,
                            resolved_at TIMESTAMPTZ,
                            notes TEXT,
                            created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =============================================
-- INDEXES
-- =============================================

-- Scheduler query: find overdue users fast
CREATE INDEX idx_checkins_user_alert_time
    ON check_ins(user_id, alert_sent, checked_in_at DESC);

-- Get latest check-in per user
CREATE INDEX idx_checkins_user_time
    ON check_ins(user_id, checked_in_at DESC);

-- SOS active events
CREATE INDEX idx_sos_unresolved
    ON sos_events(user_id, resolved, triggered_at DESC);



-- Refresh token table for secure token rotation
CREATE TABLE refresh_tokens (
                                id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                                user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                token_hash  VARCHAR(255) NOT NULL UNIQUE,
                                expires_at  TIMESTAMPTZ NOT NULL,
                                revoked     BOOLEAN     NOT NULL DEFAULT FALSE,
                                created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_user
    ON refresh_tokens(user_id, revoked, expires_at);