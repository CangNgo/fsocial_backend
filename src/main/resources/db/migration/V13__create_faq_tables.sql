-- FAQ / Tutorial / About us content, kèm bảng phụ ghi lượt xem và đánh giá
-- để thống kê chính xác (không đếm dồn cột counter trên faq).

CREATE TABLE faq (
    id            VARCHAR(36) PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    description   TEXT,
    content       TEXT NOT NULL,
    type          VARCHAR(16) NOT NULL,
    attachment_id VARCHAR(36) REFERENCES attachments(id),
    created_by    VARCHAR(36),
    updated_by    VARCHAR(36),
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP
);

CREATE INDEX idx_faq_type ON faq(type);

CREATE TABLE faq_view (
    id        VARCHAR(36) PRIMARY KEY,
    faq_id    VARCHAR(36) NOT NULL REFERENCES faq(id) ON DELETE CASCADE,
    user_id   VARCHAR(36),
    viewed_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_faq_view_faq ON faq_view(faq_id);

CREATE TABLE faq_rating (
    faq_id     VARCHAR(36) NOT NULL REFERENCES faq(id) ON DELETE CASCADE,
    user_id    VARCHAR(36) NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    score      SMALLINT NOT NULL CHECK (score BETWEEN 1 AND 5),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP,
    PRIMARY KEY (faq_id, user_id)
);
