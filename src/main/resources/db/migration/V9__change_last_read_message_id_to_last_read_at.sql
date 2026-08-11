ALTER TABLE conversation_members
    DROP COLUMN last_read_message_id,
    ADD COLUMN last_read_at TIMESTAMP;
