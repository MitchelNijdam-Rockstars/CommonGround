CREATE TABLE user_expertise
(
    user_id  BIGINT NOT NULL REFERENCES app_user (id),
    label_id BIGINT NOT NULL REFERENCES label (id),
    PRIMARY KEY (user_id, label_id)
);
