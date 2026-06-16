-- Candidate Patterns submitted together with a TopicSuggestion. They have no language of their
-- own (it lives on the suggestion/Topic) and become real Patterns when the suggestion is approved.
CREATE TABLE topic_suggestion_pattern
(
    id                  BIGSERIAL PRIMARY KEY,
    topic_suggestion_id BIGINT       REFERENCES topic_suggestion (id),
    title               VARCHAR(200),
    code                TEXT         NOT NULL,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_topic_suggestion_pattern_suggestion ON topic_suggestion_pattern (topic_suggestion_id);
