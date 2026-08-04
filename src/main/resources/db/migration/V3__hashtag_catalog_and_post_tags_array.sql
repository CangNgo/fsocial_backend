ALTER TABLE post ADD COLUMN tags TEXT[] NOT NULL DEFAULT '{}';

UPDATE post p
SET tags = COALESCE((
    SELECT array_agg(pt.tag ORDER BY pt.tag)
    FROM post_tag pt WHERE pt.post_id = p.id
), '{}');

CREATE INDEX idx_post_tags_gin ON post USING GIN (tags);

DROP TABLE post_tag;

CREATE TABLE hashtag (
    name        VARCHAR(64) PRIMARY KEY,
    usage_count INT NOT NULL DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP NOT NULL DEFAULT now()
);

INSERT INTO hashtag (name, usage_count, created_at, updated_at)
SELECT t AS name, count(*), now(), now()
FROM post, unnest(tags) AS t
WHERE status = true
GROUP BY t;
