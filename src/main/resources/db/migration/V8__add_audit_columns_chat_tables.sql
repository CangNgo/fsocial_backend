-- AbstractEntity requires created_by/updated_by/updated_at on every entity; V7 missed them on
-- conversations, messages, message_attachments (conversation_members already had them).

ALTER TABLE conversations
    ADD COLUMN updated_by VARCHAR(36),
    ADD COLUMN updated_at TIMESTAMP;

ALTER TABLE messages
    ADD COLUMN created_by VARCHAR(36),
    ADD COLUMN updated_by VARCHAR(36),
    ADD COLUMN updated_at TIMESTAMP;

ALTER TABLE message_attachments
    ADD COLUMN created_by VARCHAR(36),
    ADD COLUMN updated_by VARCHAR(36),
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT now(),
    ADD COLUMN updated_at TIMESTAMP;
