-- Optional manual schema for DB-backed operational audit logging.
-- The application can also create this table through Hibernate ddl-auto=update.
-- Retention policy: keep 180 days by default, or set APP_AUDIT_RETENTION_DAYS.
-- For manual cleanup, run:
-- DELETE FROM audit_logs WHERE occurred_at < DATE_SUB(UTC_TIMESTAMP(6), INTERVAL 180 DAY);

CREATE TABLE audit_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_type VARCHAR(48) NOT NULL,
    outcome VARCHAR(16) NOT NULL,
    http_method VARCHAR(12) NOT NULL,
    request_path VARCHAR(512) NOT NULL,
    status_code INT NOT NULL,
    user_id INT NULL,
    client_ip VARCHAR(64) NULL,
    user_agent VARCHAR(512) NULL,
    occurred_at DATETIME(6) NOT NULL,
    failure_reason VARCHAR(128) NULL,
    PRIMARY KEY (id),
    INDEX idx_audit_logs_occurred_at (occurred_at),
    INDEX idx_audit_logs_user_id_occurred_at (user_id, occurred_at),
    INDEX idx_audit_logs_event_type_occurred_at (event_type, occurred_at),
    INDEX idx_audit_logs_outcome_occurred_at (outcome, occurred_at)
);
