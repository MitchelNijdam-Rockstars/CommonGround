CREATE TABLE pattern_suggestion
(
    id                 BIGSERIAL PRIMARY KEY,
    topic_id           BIGINT      NOT NULL REFERENCES topic (id),
    user_id            BIGINT      NOT NULL REFERENCES app_user (id),
    title              VARCHAR(200),
    code               TEXT        NOT NULL,
    language           VARCHAR(50) NOT NULL,
    status             VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    rejection_reason   VARCHAR(500),
    created_pattern_id BIGINT REFERENCES pattern (id),
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    reviewed_at        TIMESTAMPTZ
);

CREATE INDEX idx_pattern_suggestion_status ON pattern_suggestion (status);
CREATE INDEX idx_pattern_suggestion_user ON pattern_suggestion (user_id);
