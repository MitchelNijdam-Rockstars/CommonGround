CREATE TABLE pattern
(
    id           BIGSERIAL PRIMARY KEY,
    topic_id     BIGINT           NOT NULL REFERENCES topic (id),
    title        VARCHAR(200)     NOT NULL,
    code         TEXT             NOT NULL,
    language     VARCHAR(50)      NOT NULL,
    elo_rating   DOUBLE PRECISION NOT NULL DEFAULT 1500,
    times_shown  INT              NOT NULL DEFAULT 0,
    times_chosen INT              NOT NULL DEFAULT 0,
    active       BOOLEAN          NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ      NOT NULL DEFAULT now()
);

CREATE INDEX idx_pattern_topic_active ON pattern (topic_id, active);
