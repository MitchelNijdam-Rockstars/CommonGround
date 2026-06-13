CREATE TABLE vote
(
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT      NOT NULL REFERENCES app_user (id),
    topic_id          BIGINT      NOT NULL REFERENCES topic (id),
    winner_pattern_id BIGINT      NOT NULL REFERENCES pattern (id),
    loser_pattern_id  BIGINT      NOT NULL REFERENCES pattern (id),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_vote_topic ON vote (topic_id);
CREATE INDEX idx_vote_user ON vote (user_id);

CREATE TABLE skip
(
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT      NOT NULL REFERENCES app_user (id),
    topic_id     BIGINT      NOT NULL REFERENCES topic (id),
    pattern_a_id BIGINT      NOT NULL REFERENCES pattern (id),
    pattern_b_id BIGINT      NOT NULL REFERENCES pattern (id),
    reason       VARCHAR(30) NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_skip_user ON skip (user_id);
