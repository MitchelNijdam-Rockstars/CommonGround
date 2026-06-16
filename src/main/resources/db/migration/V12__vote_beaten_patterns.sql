-- A Vote now records one winner that beat a SET of patterns (the other options shown),
-- instead of a single loser. Pairwise votes are the special case of one beaten pattern.
CREATE TABLE vote_beaten_pattern
(
    vote_id    BIGINT NOT NULL REFERENCES vote (id),
    pattern_id BIGINT NOT NULL REFERENCES pattern (id),
    PRIMARY KEY (vote_id, pattern_id)
);

CREATE INDEX idx_vote_beaten_pattern_pattern ON vote_beaten_pattern (pattern_id);

-- Backfill: every existing vote beat exactly its former loser.
INSERT INTO vote_beaten_pattern (vote_id, pattern_id)
SELECT id, loser_pattern_id
FROM vote;

ALTER TABLE vote
    DROP COLUMN loser_pattern_id;
