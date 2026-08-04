ALTER TABLE attachments
    ADD COLUMN post_id VARCHAR(36) REFERENCES post(id) ON DELETE CASCADE,
    ADD COLUMN ord INT,
    ADD COLUMN width INT,
    ADD COLUMN height INT,
    ADD COLUMN type VARCHAR(16),
    ADD COLUMN media_type VARCHAR(8);

INSERT INTO attachments (id, url, post_id, ord, width, height, type, media_type, created_at)
SELECT id, url, post_id, ord, width, height, 'POST', upper(type), now()
FROM post_media;

UPDATE comment_media SET type = upper(type) WHERE type IS NOT NULL;

CREATE INDEX idx_attachments_post ON attachments(post_id, ord);
CREATE INDEX idx_attachments_type ON attachments(type);

DROP TABLE post_media;
