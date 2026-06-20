CREATE TABLE IF NOT EXISTS branches (
    id           VARCHAR(36)  PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    franchise_id VARCHAR(36)  NOT NULL REFERENCES franchises(id)
);
