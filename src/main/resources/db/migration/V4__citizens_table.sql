CREATE TABLE IF NOT EXISTS citizens (
    nic         VARCHAR(20)  PRIMARY KEY,
    full_name   VARCHAR(255) NOT NULL,
    date_of_birth DATE,
    gender      VARCHAR(20),
    address     TEXT,
    alive       BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_citizens_alive ON citizens(alive);
