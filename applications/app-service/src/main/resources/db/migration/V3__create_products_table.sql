CREATE TABLE IF NOT EXISTS products (
    id        VARCHAR(36)  PRIMARY KEY,
    name      VARCHAR(255) NOT NULL,
    stock     NUMERIC      NOT NULL,
    branch_id VARCHAR(36)  NOT NULL REFERENCES branches(id)
);
