-- One-time MySQL 8 migration for OAuth login.
-- Review existing constraints and backups before applying in production.
-- oauth_provider supports provider enum values such as GOOGLE and NAVER.

ALTER TABLE users
    MODIFY password VARCHAR(255) NULL,
    MODIFY nickname VARCHAR(24) NULL,
    MODIFY gender VARCHAR(1) NULL,
    MODIFY birth_date DATE NULL,
    MODIFY phone_number VARCHAR(15) NULL;

ALTER TABLE users
    ADD COLUMN oauth_provider VARCHAR(24) NULL,
    ADD COLUMN oauth_provider_id VARCHAR(128) NULL,
    ADD COLUMN profile_completed BOOLEAN NOT NULL DEFAULT TRUE;

CREATE UNIQUE INDEX uk_users_oauth_provider_id
    ON users (oauth_provider, oauth_provider_id);
