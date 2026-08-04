-- postService — schema PostgreSQL (migrate từ MongoDB)
-- Quy ước: PK VARCHAR(36) = UUID; audit created_by/updated_by/created_at/updated_at.
-- Mọi embedded array/map của Mongo được chuẩn hóa thành bảng riêng.

-- ============================================================
-- Identity & quan hệ
-- ============================================================

CREATE TABLE permission (
    name        VARCHAR(64) PRIMARY KEY,
    description TEXT
);

CREATE TABLE role (
    id          VARCHAR(36) PRIMARY KEY,
    name        VARCHAR(64) NOT NULL UNIQUE,
    description TEXT,
    created_by  VARCHAR(36),
    updated_by  VARCHAR(36),
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP
);

CREATE TABLE role_permission (
    role_id         VARCHAR(36) NOT NULL REFERENCES role(id) ON DELETE CASCADE,
    permission_name VARCHAR(64) NOT NULL REFERENCES permission(name) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_name)
);

CREATE TABLE account (
    id           VARCHAR(36) PRIMARY KEY,
    username     VARCHAR(64) NOT NULL UNIQUE,
    password     VARCHAR(255),
    first_name   VARCHAR(128),
    last_name    VARCHAR(128),
    display_name VARCHAR(255),
    dob          DATE,
    gender       INT NOT NULL DEFAULT 0,
    avatar       TEXT,
    background   TEXT,
    bio          TEXT,
    address      TEXT,
    email        VARCHAR(255),
    is_kol       BOOLEAN NOT NULL DEFAULT FALSE,
    is_public    BOOLEAN NOT NULL DEFAULT TRUE,
    status       BOOLEAN NOT NULL DEFAULT TRUE,
    provider     VARCHAR(16) NOT NULL DEFAULT 'LOCAL',
    google_id    VARCHAR(64),
    role_id      VARCHAR(36) REFERENCES role(id),
    created_by   VARCHAR(36),
    updated_by   VARCHAR(36),
    created_at   TIMESTAMP,
    updated_at   TIMESTAMP
);

CREATE INDEX idx_account_created  ON account(created_at);
CREATE INDEX idx_account_google_id ON account(google_id);

-- Account.follower / Account.following (Set<String>) -> 1 bảng cạnh
CREATE TABLE follow (
    follower_id VARCHAR(36) NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    followee_id VARCHAR(36) NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (follower_id, followee_id),
    CHECK (follower_id <> followee_id)
);

CREATE INDEX idx_follow_followee ON follow(followee_id);

CREATE TABLE token (
    id         VARCHAR(36) PRIMARY KEY,
    token      TEXT,
    account_id VARCHAR(36) UNIQUE REFERENCES account(id) ON DELETE CASCADE
);

CREATE TABLE refresh_token (
    id          VARCHAR(36) PRIMARY KEY,
    token       TEXT NOT NULL UNIQUE,
    username    VARCHAR(64) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    user_agent  TEXT,
    ip_address  VARCHAR(64)
);

CREATE INDEX idx_refresh_username_expiry ON refresh_token(username, expiry_date);

-- ============================================================
-- Post — tags là cột mảng native (không còn post_tag); media dùng chung
-- bảng attachments (không còn post_media riêng)
-- ============================================================

CREATE TABLE post (
    id              VARCHAR(36) PRIMARY KEY,
    owner_id        VARCHAR(36) NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    text            TEXT,
    html            TEXT,
    create_datetime TIMESTAMP NOT NULL DEFAULT now(),
    origin_post_id  VARCHAR(36) REFERENCES post(id) ON DELETE SET NULL,
    is_share        BOOLEAN NOT NULL DEFAULT FALSE,
    status          BOOLEAN NOT NULL DEFAULT TRUE,
    global_score    DOUBLE PRECISION NOT NULL DEFAULT 0,
    raw_engagement  DOUBLE PRECISION NOT NULL DEFAULT 0,
    share_count     INT NOT NULL DEFAULT 0,
    tags            TEXT[] NOT NULL DEFAULT '{}',
    created_by      VARCHAR(36),
    updated_by      VARCHAR(36),
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP
);

CREATE INDEX idx_post_owner_time ON post(owner_id, create_datetime DESC);
CREATE INDEX idx_post_created    ON post(create_datetime DESC);
CREATE INDEX idx_post_score      ON post(global_score DESC) WHERE status;
CREATE INDEX idx_post_created_at ON post(created_at);
CREATE INDEX idx_post_tags       ON post USING GIN(tags);

-- ============================================================
-- Attachments — thư viện media dùng chung; khi post_id IS NOT NULL đóng vai
-- trò post_media cũ (ord/width/height/type/media_type)
-- ============================================================

CREATE TABLE attachments (
    id            VARCHAR(36) PRIMARY KEY,
    public_id     VARCHAR(255),
    resource_type VARCHAR(64),
    file_type     VARCHAR(64),
    size          VARCHAR(64),
    url           TEXT,
    owner_id      VARCHAR(36),
    post_id       VARCHAR(36) REFERENCES post(id) ON DELETE CASCADE,
    ord           INT,
    width         INT,
    height        INT,
    type          VARCHAR(16),
    media_type    VARCHAR(8),
    created_by    VARCHAR(36),
    updated_by    VARCHAR(36),
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP
);

CREATE INDEX idx_attachments_owner ON attachments(owner_id);
CREATE INDEX idx_attachments_post  ON attachments(post_id, ord);

-- Catalog hashtag — đồng bộ định kỳ từ post.tags qua HashtagSyncScheduler
CREATE TABLE hashtag (
    name        VARCHAR(64) PRIMARY KEY,
    usage_count INT NOT NULL DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE post_like (
    post_id    VARCHAR(36) NOT NULL REFERENCES post(id) ON DELETE CASCADE,
    user_id    VARCHAR(36) NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (post_id, user_id)
);

CREATE INDEX idx_post_like_user ON post_like(user_id);

-- ============================================================
-- Comment — gộp cả reply qua parent_id (bỏ embedded replies[]).
-- post_id KHÔNG có FK: cleanup khi xóa post chạy bất đồng bộ qua
-- RabbitMQ CommentConsumer (post/comment không còn cùng transaction).
-- ============================================================

CREATE TABLE comment (
    id              VARCHAR(36) PRIMARY KEY,
    post_id         VARCHAR(36) NOT NULL,
    parent_id       VARCHAR(36) REFERENCES comment(id) ON DELETE CASCADE,
    user_id         VARCHAR(36) NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    text            TEXT,
    html            TEXT,
    create_datetime TIMESTAMP NOT NULL DEFAULT now(),
    created_by      VARCHAR(36),
    updated_by      VARCHAR(36),
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP
);

CREATE INDEX idx_comment_post_root ON comment(post_id) WHERE parent_id IS NULL;
CREATE INDEX idx_comment_post      ON comment(post_id);
CREATE INDEX idx_comment_parent    ON comment(parent_id);

CREATE TABLE comment_media (
    id         VARCHAR(36) PRIMARY KEY,
    comment_id VARCHAR(36) NOT NULL REFERENCES comment(id) ON DELETE CASCADE,
    ord        INT NOT NULL DEFAULT 0,
    url        TEXT NOT NULL,
    type       VARCHAR(16),
    width      INT,
    height     INT
);

CREATE INDEX idx_comment_media_comment ON comment_media(comment_id, ord);

CREATE TABLE comment_like (
    comment_id VARCHAR(36) NOT NULL REFERENCES comment(id) ON DELETE CASCADE,
    user_id    VARCHAR(36) NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (comment_id, user_id)
);

CREATE INDEX idx_comment_like_user ON comment_like(user_id);

-- ============================================================
-- Notification — recipient/sender là id thuần (không FK), metadata &
-- aggregated senders tách bảng
-- ============================================================

CREATE TABLE notification (
    id           VARCHAR(36) PRIMARY KEY,
    recipient_id VARCHAR(36) NOT NULL,
    sender_id    VARCHAR(36),
    type         VARCHAR(32) NOT NULL,
    group_key    VARCHAR(128),
    title        TEXT,
    body         TEXT,
    is_read      BOOLEAN NOT NULL DEFAULT FALSE,
    pushed       BOOLEAN NOT NULL DEFAULT FALSE,
    read_at      TIMESTAMP,
    created_at   TIMESTAMP,
    updated_at   TIMESTAMP
);

CREATE INDEX idx_notif_recipient_read ON notification(recipient_id, is_read, created_at DESC);
CREATE INDEX idx_notif_recipient_time ON notification(recipient_id, created_at DESC, id DESC);
CREATE INDEX idx_notif_group          ON notification(recipient_id, group_key, created_at DESC);

CREATE TABLE notification_metadata (
    notification_id VARCHAR(36) NOT NULL REFERENCES notification(id) ON DELETE CASCADE,
    meta_key        VARCHAR(64) NOT NULL,
    meta_value      TEXT,
    PRIMARY KEY (notification_id, meta_key)
);

-- aggregated_sender_ids[] có thứ tự -> PK theo (notification_id, ord)
CREATE TABLE notification_sender (
    notification_id VARCHAR(36) NOT NULL REFERENCES notification(id) ON DELETE CASCADE,
    sender_id       VARCHAR(36) NOT NULL,
    ord             INT NOT NULL,
    PRIMARY KEY (notification_id, ord)
);

CREATE TABLE notification_preference (
    id                VARCHAR(36) PRIMARY KEY,
    user_id           VARCHAR(36) NOT NULL UNIQUE,
    quiet_hours_start VARCHAR(8),
    quiet_hours_end   VARCHAR(8),
    timezone          VARCHAR(64),
    created_by        VARCHAR(36),
    updated_by        VARCHAR(36),
    created_at        TIMESTAMP,
    updated_at        TIMESTAMP
);

CREATE TABLE preference_setting (
    preference_id     VARCHAR(36) NOT NULL REFERENCES notification_preference(id) ON DELETE CASCADE,
    notification_type VARCHAR(32) NOT NULL,
    push_enabled      BOOLEAN NOT NULL DEFAULT TRUE,
    email_enabled     BOOLEAN NOT NULL DEFAULT FALSE,
    in_app_enabled    BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (preference_id, notification_type)
);

CREATE TABLE notification_template (
    id         VARCHAR(36) PRIMARY KEY,
    type       VARCHAR(32) NOT NULL UNIQUE,
    is_active  BOOLEAN NOT NULL DEFAULT TRUE,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE template_translation (
    template_id    VARCHAR(36) NOT NULL REFERENCES notification_template(id) ON DELETE CASCADE,
    locale         VARCHAR(8) NOT NULL,
    title_template TEXT,
    body_template  TEXT,
    PRIMARY KEY (template_id, locale)
);

CREATE TABLE template_default_data (
    template_id VARCHAR(36) NOT NULL REFERENCES notification_template(id) ON DELETE CASCADE,
    data_key    VARCHAR(64) NOT NULL,
    data_value  TEXT,
    PRIMARY KEY (template_id, data_key)
);

CREATE TABLE device_token (
    id          VARCHAR(36) PRIMARY KEY,
    user_id     VARCHAR(36) NOT NULL,
    fcm_token   TEXT NOT NULL UNIQUE,
    device_id   VARCHAR(128) NOT NULL,
    device_type VARCHAR(32),
    device_name VARCHAR(128),
    app_version VARCHAR(32),
    os_version  VARCHAR(32),
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    last_use_at TIMESTAMP,
    created_by  VARCHAR(36),
    updated_by  VARCHAR(36),
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    CONSTRAINT uk_device_user_device UNIQUE (user_id, device_id)
);

CREATE INDEX idx_device_user_active ON device_token(user_id, is_active);

-- ============================================================
-- Feed / interest graph
-- ============================================================

-- UserInterests.interests[] -> mỗi tag một dòng
CREATE TABLE user_interest (
    user_id    VARCHAR(36) NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    tag        VARCHAR(64) NOT NULL,
    weight     DOUBLE PRECISION NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, tag)
);

CREATE INDEX idx_user_interest_weight ON user_interest(user_id, weight DESC);
CREATE INDEX idx_user_interest_tag    ON user_interest(tag);

-- Mongo TTL index không có tương đương -> dọn bằng SeenPostPurgeScheduler
CREATE TABLE seen_post (
    user_id VARCHAR(36) NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    post_id VARCHAR(36) NOT NULL REFERENCES post(id) ON DELETE CASCADE,
    seen_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, post_id)
);

CREATE INDEX idx_seen_post_seen_at ON seen_post(seen_at);

-- ============================================================
-- Complaint / term / email template
-- ============================================================

CREATE TABLE term_of_service (
    id         VARCHAR(36) PRIMARY KEY,
    name       TEXT NOT NULL,
    status     BOOLEAN NOT NULL DEFAULT TRUE,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE complaint (
    id             VARCHAR(36) PRIMARY KEY,
    target_id      VARCHAR(36) NOT NULL,
    complaint_type VARCHAR(16) NOT NULL,
    is_read        BOOLEAN NOT NULL DEFAULT FALSE,
    created_by     VARCHAR(36),
    updated_by     VARCHAR(36),
    created_at     TIMESTAMP,
    updated_at     TIMESTAMP
);

CREATE INDEX idx_complaint_target ON complaint(target_id);

-- Complaint.details[] -> bảng con
CREATE TABLE complaint_detail (
    id                  VARCHAR(36) PRIMARY KEY,
    complaint_id        VARCHAR(36) NOT NULL REFERENCES complaint(id) ON DELETE CASCADE,
    user_id             VARCHAR(36) NOT NULL,
    term_of_service_id  VARCHAR(36) REFERENCES term_of_service(id) ON DELETE SET NULL,
    create_datetime     TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_complaint_detail_complaint ON complaint_detail(complaint_id);
CREATE INDEX idx_complaint_detail_created   ON complaint_detail(create_datetime);

CREATE TABLE email_template (
    id         VARCHAR(36) PRIMARY KEY,
    name       VARCHAR(128) NOT NULL,
    content    TEXT,
    is_active  BOOLEAN NOT NULL DEFAULT TRUE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE email_template_field (
    id          VARCHAR(36) PRIMARY KEY,
    name        VARCHAR(128) NOT NULL,
    description TEXT,
    created_by  VARCHAR(36),
    updated_by  VARCHAR(36),
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP
);
