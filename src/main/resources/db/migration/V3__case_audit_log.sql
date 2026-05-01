CREATE TABLE IF NOT EXISTS case_audit_log (
    id            BIGSERIAL PRIMARY KEY,
    death_case_id BIGINT       NOT NULL REFERENCES death_cases(id),
    action        VARCHAR(100) NOT NULL,
    performed_by_username VARCHAR(255) NOT NULL,
    from_status   VARCHAR(100),
    to_status     VARCHAR(100),
    performed_at  TIMESTAMP    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_case_audit_log_case ON case_audit_log(death_case_id);
