CREATE TABLE label
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL UNIQUE,
    label_type VARCHAR(30)  NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE topic
(
    id         BIGSERIAL PRIMARY KEY,
    question   VARCHAR(500) NOT NULL,
    context    TEXT,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE topic_label
(
    topic_id BIGINT NOT NULL REFERENCES topic (id),
    label_id BIGINT NOT NULL REFERENCES label (id),
    PRIMARY KEY (topic_id, label_id)
);
