CREATE TABLE posts (
                       id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                       user_id         UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                       title           VARCHAR(200) NOT NULL,
                       content         TEXT        NOT NULL,
                       visibility      VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
                       location        GEOGRAPHY(POINT, 4326),
                       moderation_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                       moderation_reason TEXT,
                       likes_count     INTEGER     NOT NULL DEFAULT 0,
                       created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                       updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_posts_visibility_created
    ON posts(visibility, created_at DESC);

CREATE INDEX idx_posts_user_created
    ON posts(user_id, created_at DESC);

CREATE INDEX idx_posts_location
    ON posts USING GIST(location);

CREATE INDEX idx_posts_moderation_status
    ON posts(moderation_status);