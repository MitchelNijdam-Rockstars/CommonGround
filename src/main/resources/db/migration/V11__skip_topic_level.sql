-- A Skip now applies to the whole topic shown, not a single pair of patterns.
ALTER TABLE skip
    DROP COLUMN pattern_a_id,
    DROP COLUMN pattern_b_id;
