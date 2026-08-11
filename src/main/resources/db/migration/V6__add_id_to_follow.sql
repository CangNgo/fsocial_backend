-- Follow entity giờ extends AbstractEntity (id, created_by, updated_by, updated_at)
-- thay vì composite PK (follower_id, followee_id). Đổi bảng follow cho khớp.

ALTER TABLE follow DROP CONSTRAINT follow_pkey;

ALTER TABLE follow
    ADD COLUMN id         VARCHAR(36),
    ADD COLUMN created_by VARCHAR(36),
    ADD COLUMN updated_by VARCHAR(36),
    ADD COLUMN updated_at TIMESTAMP;

UPDATE follow SET id = gen_random_uuid()::text WHERE id IS NULL;

ALTER TABLE follow
    ALTER COLUMN id SET NOT NULL,
    ADD CONSTRAINT follow_pkey PRIMARY KEY (id);

ALTER TABLE follow
    ADD CONSTRAINT uk_follow_follower_followee UNIQUE (follower_id, followee_id);
