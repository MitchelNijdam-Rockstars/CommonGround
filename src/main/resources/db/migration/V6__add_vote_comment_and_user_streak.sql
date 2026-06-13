ALTER TABLE vote
    ADD COLUMN comment VARCHAR(500);

ALTER TABLE app_user
    ADD COLUMN last_voted_date DATE,
    ADD COLUMN current_streak  INT NOT NULL DEFAULT 0;
