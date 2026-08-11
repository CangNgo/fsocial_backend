-- Chat feature: conversations (DIRECT/GROUP), members, messages, attachments.
-- Ids/FKs VARCHAR(36), datetimes TIMESTAMP — matches V1__init_schema.sql / AbstractEntity convention.

CREATE TABLE conversations (
    id         VARCHAR(36) PRIMARY KEY,
    type       VARCHAR(16) NOT NULL,
    name       VARCHAR(100),
    avatar_url TEXT,
    created_by VARCHAR(36) REFERENCES account(id),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE conversation_members (
    id                    VARCHAR(36) PRIMARY KEY,
    conversation_id       VARCHAR(36) NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    user_id               VARCHAR(36) NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    role                  VARCHAR(16) NOT NULL DEFAULT 'MEMBER',
    last_read_message_id  VARCHAR(36),
    is_muted              BOOLEAN NOT NULL DEFAULT FALSE,
    joined_at             TIMESTAMP NOT NULL DEFAULT now(),
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP,
    CONSTRAINT uk_conv_member UNIQUE (conversation_id, user_id)
);

CREATE TABLE messages (
    id              VARCHAR(36) PRIMARY KEY,
    conversation_id VARCHAR(36) NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    sender_id       VARCHAR(36) NOT NULL REFERENCES account(id),
    content         TEXT,
    message_type    VARCHAR(16) NOT NULL DEFAULT 'TEXT',
    reply_to_id     VARCHAR(36) REFERENCES messages(id),
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMP
);
CREATE INDEX idx_messages_conversation_time ON messages(conversation_id, created_at);

CREATE TABLE message_attachments (
    id               VARCHAR(36) PRIMARY KEY,
    message_id       VARCHAR(36) NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    attachment_type  VARCHAR(16) NOT NULL,
    url              TEXT NOT NULL,
    thumbnail_url    TEXT,
    file_name        VARCHAR(255),
    file_size        BIGINT,
    mime_type        VARCHAR(100),
    width            INT,
    height           INT,
    duration_seconds INT
);
