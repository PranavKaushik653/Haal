CREATE TABLE connections (
                             id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                             user_id         UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                             other_user_id   UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                             connected_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                             UNIQUE(user_id, other_user_id),
                             CHECK (user_id != other_user_id)
    );

CREATE INDEX idx_connections_user
    ON connections(user_id, connected_at DESC);

CREATE TABLE post_likes (
                            id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                            post_id     UUID        NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
                            user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                            UNIQUE(post_id, user_id)
);

CREATE INDEX idx_post_likes_user
    ON post_likes(user_id, created_at DESC);