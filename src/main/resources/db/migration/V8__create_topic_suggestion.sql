CREATE TABLE topic_suggestion
(
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT       NOT NULL REFERENCES app_user (id),
    question         VARCHAR(500) NOT NULL,
    context          TEXT,
    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    rejection_reason VARCHAR(500),
    created_topic_id BIGINT REFERENCES topic (id),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    reviewed_at      TIMESTAMPTZ
);

CREATE INDEX idx_topic_suggestion_status ON topic_suggestion (status);
CREATE INDEX idx_topic_suggestion_user ON topic_suggestion (user_id);

CREATE TABLE topic_suggestion_label
(
    topic_suggestion_id BIGINT NOT NULL REFERENCES topic_suggestion (id),
    label_id            BIGINT NOT NULL REFERENCES label (id),
    PRIMARY KEY (topic_suggestion_id, label_id)
);
