CREATE TABLE parties_demo (
    id VARCHAR(36) PRIMARY KEY,
    payload TEXT NOT NULL,
    created_at BIGINT NOT NULL,
    version BIGINT NOT NULL
);
