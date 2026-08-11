-- Tách password/provider/google_id từ account sang bảng auth_provider (1-N).
-- username chuyển nullable (Google-only account không có username/password).

CREATE TABLE auth_provider (
    id               VARCHAR(36) PRIMARY KEY,
    user_id          VARCHAR(36) NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    provider         VARCHAR(16) NOT NULL,
    password         VARCHAR(255),
    provider_user_id VARCHAR(255),
    created_by       VARCHAR(36),
    updated_by       VARCHAR(36),
    created_at       TIMESTAMP,
    updated_at       TIMESTAMP,
    CONSTRAINT uk_auth_provider_account_provider UNIQUE (user_id, provider)
);

CREATE UNIQUE INDEX ux_auth_provider_provider_user
    ON auth_provider(provider, provider_user_id)
    WHERE provider_user_id IS NOT NULL;

-- Backfill: mỗi account hiện có -> 1 dòng auth_provider theo provider hiện tại
INSERT INTO auth_provider (id, user_id, provider, password, provider_user_id, created_at, updated_at)
SELECT gen_random_uuid()::text, id, provider, password, google_id, created_at, updated_at
FROM account;

DROP INDEX IF EXISTS idx_account_google_id;

ALTER TABLE account
    DROP COLUMN password,
    DROP COLUMN provider,
    DROP COLUMN google_id;

-- username: NOT NULL UNIQUE -> nullable + partial unique (Google account không có username)
ALTER TABLE account ALTER COLUMN username DROP NOT NULL;
ALTER TABLE account DROP CONSTRAINT account_username_key;
CREATE UNIQUE INDEX ux_account_username ON account(username) WHERE username IS NOT NULL;

-- refresh_token: username-keyed -> account_id-keyed. Bảng session tạm thời, TRUNCATE
-- thay vì join username->id (chấp nhận: user đang có refresh token phải login lại).
TRUNCATE TABLE refresh_token;

DROP INDEX IF EXISTS idx_refresh_username_expiry;
ALTER TABLE refresh_token DROP COLUMN username;
ALTER TABLE refresh_token ADD COLUMN account_id VARCHAR(36) NOT NULL REFERENCES account(id) ON DELETE CASCADE;

CREATE INDEX idx_refresh_account_expiry ON refresh_token(account_id, expiry_date);
