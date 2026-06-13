CREATE TABLE app_user
(
    id         BIGSERIAL PRIMARY KEY,
    email      VARCHAR(320) NOT NULL UNIQUE,
    role       VARCHAR(20)  NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
