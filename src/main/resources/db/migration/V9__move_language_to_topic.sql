-- Language is a property of the Topic, not of each individual Pattern: every Pattern competing
-- within a Topic is written in the same language. Move it up to the Topic (and to the
-- TopicSuggestion that creates one), backfilling existing Topics from their Patterns.

ALTER TABLE topic ADD COLUMN language VARCHAR(50);
ALTER TABLE topic_suggestion ADD COLUMN language VARCHAR(50);

UPDATE topic t
SET language = (
    SELECT p.language
    FROM pattern p
    WHERE p.topic_id = t.id
    ORDER BY p.id
    LIMIT 1
);

ALTER TABLE pattern DROP COLUMN language;
ALTER TABLE pattern_suggestion DROP COLUMN language;
