# postService — Báo cáo Schema PostgreSQL

Mô tả toàn bộ 33 bảng, entity JPA tương ứng, nhiệm vụ từng field và quan hệ giữa
các bảng. Sinh ra từ migration Mongo → Postgres (xem `src/main/resources/db/migration/V1__init_schema.sql`
và `src/main/java/com/fsocial/postservice/entity/`).

Quy ước chung:
- PK là `VARCHAR(36)` (UUID dạng chuỗi), trừ các bảng khóa kép (composite key) liệt kê PK là 2 cột.
- Cột audit `created_by` / `updated_by` (id account thao tác) và `created_at` / `updated_at` xuất hiện ở hầu hết bảng gốc (không có ở bảng con thuần join).
- "Thay cho (Mongo)" ghi lại field/array embedded cũ để biết vì sao bảng này tồn tại.

---

## 1. Nhóm Identity & phân quyền

### `permission`
Danh mục quyền hạn tĩnh (VD: `POST_CREATE`, `USER_BAN`).

| Field | Nhiệm vụ |
|---|---|
| `name` (PK) | Tên quyền, dùng trực tiếp làm khóa (không cần UUID) |
| `description` | Mô tả quyền |

Quan hệ: 1 permission — N `role_permission`.

### `role`
Vai trò người dùng (`ROLE_USER`, `ROLE_ADMIN`, ...).

| Field | Nhiệm vụ |
|---|---|
| `id` (PK) | UUID |
| `name` | Tên vai trò, unique |
| `description` | Mô tả |

Quan hệ: 1 role — N `account` (`account.role_id`); N-N với `permission` qua `role_permission`.

### `role_permission`
Bảng nối N-N role ↔ permission (JPA: `@ManyToMany` trên `Role.permissions`).

| Field | Nhiệm vụ |
|---|---|
| `role_id` (PK, FK role) | |
| `permission_name` (PK, FK permission) | |

Xóa role/permission → cascade xóa dòng nối (`ON DELETE CASCADE`).

### `account`
Bảng người dùng trung tâm — mọi bảng khác tham chiếu tới đây.

| Field | Nhiệm vụ |
|---|---|
| `id` (PK) | UUID |
| `username` | Đăng nhập, unique |
| `password` | BCrypt hash, null nếu đăng nhập Google |
| `first_name` / `last_name` / `display_name` | Tên hiển thị |
| `dob` | Ngày sinh |
| `gender` | 0/1/... |
| `avatar` / `background` | URL ảnh Cloudinary |
| `bio` / `address` | Thông tin cá nhân |
| `email` | Email, dùng cho OTP/reset password |
| `is_kol` | Cờ tài khoản KOL (ảnh hưởng thuật toán feed) |
| `is_public` | Public/private profile — chi phối quyền xem post |
| `status` | Bật/khóa tài khoản |
| `provider` | `LOCAL` hoặc `GOOGLE` (enum `AuthProvider`) |
| `google_id` | ID Google khi đăng nhập OAuth2 |
| `role_id` (FK role) | Vai trò |
| audit 4 cột | Ai/khi nào tạo/sửa |

Index: `created_at` (thống kê theo ngày), `google_id` (lookup OAuth2 nhanh).

Quan hệ: gốc của hầu hết bảng — `post.owner_id`, `comment.user_id`, `post_like.user_id`,
`notification.recipient_id`, `follow.*`, `user_interest.user_id`, `seen_post.user_id`, ...
Thay cho field `Account.follower`/`Account.following` (Set&lt;String&gt; embedded) — nay tách hẳn bảng `follow`.

### `follow`
Quan hệ theo dõi 1 chiều (A follow B ≠ B follow A). Thay cho 2 field Set embedded của Mongo.

| Field | Nhiệm vụ |
|---|---|
| `follower_id` (PK, FK account) | Người đi follow |
| `followee_id` (PK, FK account) | Người được follow |
| `created_at` | Thời điểm follow |

Ràng buộc `CHECK (follower_id <> followee_id)` — không tự follow chính mình.
Index `followee_id` — phục vụ truy vấn "ai đang follow tôi" / đếm follower.
Xóa account → cascade xóa mọi dòng follow liên quan (cả 2 chiều).

### `token`
Access token hiện hành của 1 account (JWT access token lưu lại để kiểm tra/blacklist).

| Field | Nhiệm vụ |
|---|---|
| `id` (PK) | UUID |
| `token` | Chuỗi JWT |
| `account_id` (FK account, UNIQUE) | 1-1 với account |

Quan hệ: `@OneToOne` — mỗi account có tối đa 1 token hiện hành; xóa account → xóa token.

### `refresh_token`
Refresh token (JWT dài hạn, 7 ngày), độc lập bảng vì có nhiều token/1 user (đa thiết bị, tối đa 5 — logic evict trong service).

| Field | Nhiệm vụ |
|---|---|
| `id` (PK) | UUID |
| `token` | Chuỗi token, unique |
| `username` | Chủ sở hữu (tra cứu theo username, không FK cứng để tránh phụ thuộc vòng khi rotate) |
| `expiry_date` | Hạn dùng |
| `user_agent` / `ip_address` | Thông tin thiết bị, phục vụ audit đăng nhập |

Index `(username, expiry_date)` — phục vụ logic loại bỏ token cũ nhất khi vượt quá 5 token/user.

---

## 2. Nhóm Post (bài viết)

### `post`
Bảng bài viết — thay thế document `Post` embedded `Content`/`MediaItem[]`/`likes[]`/`tags[]`/`ActorSnapshot` của Mongo.

| Field | Nhiệm vụ |
|---|---|
| `id` (PK) | UUID |
| `owner_id` (FK account) | Người đăng — thay `ActorSnapshot owner` (không còn lưu snapshot tên/avatar, luôn JOIN account để lấy dữ liệu mới nhất) |
| `text` / `html` | Nội dung bài viết — thay field `Content.text/html` embedded |
| `create_datetime` | Thời điểm đăng (dùng để sort feed theo mới nhất + tính time-decay của điểm số) |
| `origin_post_id` (FK post, tự tham chiếu) | Post gốc khi đây là bài share lại; `NULL` nếu là bài gốc |
| `is_share` | Cờ đánh dấu bài chia sẻ |
| `status` | Bật/ẩn bài viết (soft delete / kiểm duyệt) |
| `global_score` | Điểm xếp hạng feed — tính từ engagement + time-decay (xem `ScoringServiceImpl`) |
| `raw_engagement` | Điểm thô trước khi áp penalty thời gian, dùng để tái tính `global_score` |
| `share_count` | Số lượt share — cache đếm sẵn thay vì COUNT mỗi lần đọc |

Index: `(owner_id, create_datetime DESC)` (lấy bài của 1 user theo thời gian), `create_datetime DESC` (feed theo thời gian),
`global_score DESC WHERE status` (feed theo điểm, chỉ bài đang hiển thị), `created_at`.

Quan hệ: 1 post — N `post_media`, N `post_like`, N `post_tag`, N `comment`, N `seen_post`; tự tham chiếu qua `origin_post_id` (bài share trỏ về bài gốc).
Xóa post → cascade xóa `post_media`/`post_like`/`post_tag`/`seen_post`/`comment` (FK `ON DELETE CASCADE`); nếu post là gốc của bài share khác, `origin_post_id` của bài share được set `NULL` (`ON DELETE SET NULL`) chứ không xóa bài share.

### `post_media`
Danh sách ảnh/video đính kèm 1 post theo thứ tự. Thay cho `Post.content.mediaItems: List<MediaItem>` embedded.

| Field | Nhiệm vụ |
|---|---|
| `id` (PK) | UUID |
| `post_id` (FK post) | Post sở hữu |
| `ord` | Thứ tự hiển thị (0, 1, 2...) — thay cho vị trí trong mảng embedded cũ |
| `url` | URL Cloudinary |
| `type` | `"image"` hoặc `"video"` |
| `width` / `height` | Kích thước gốc, phục vụ layout responsive phía client |

Index `(post_id, ord)` — load đúng thứ tự khi hiển thị 1 post.

### `post_like`
Ai đã like post nào — thay cho `Post.likes: List<String>` embedded (trước đây phải đếm bằng `$size` aggregation).

| Field | Nhiệm vụ |
|---|---|
| `post_id` (PK, FK post) | |
| `user_id` (PK, FK account) | |
| `created_at` | Thời điểm like — dùng cho thông báo/audit |

PK kép (`post_id`, `user_id`) tự đảm bảo 1 user chỉ like 1 lần — thay cho việc phải check trùng phần tử trong mảng embedded. Đếm like = `COUNT(*) GROUP BY post_id`; toggle like = insert/delete 1 dòng.
Index `user_id` — lấy danh sách bài user đã like.

### `post_tag`
Tag/hashtag gắn với post — thay cho `Post.tags: List<String>` embedded.

| Field | Nhiệm vụ |
|---|---|
| `post_id` (PK, FK post) | |
| `tag` (PK) | Tên tag, không FK sang bảng riêng (tag là free-text, không cần chuẩn hóa danh mục) |

Index `tag` — thay cho compound index Mongo cũ `{tags: 1, global_score: -1}`, phục vụ JOIN feed theo tag rồi sort theo điểm.
Khi share post, tag được copy nguyên từ bài gốc bằng 1 câu `INSERT ... SELECT ... ON CONFLICT DO NOTHING` (`PostTagRepository.copyTags`), không load về tầng app.

---

## 3. Nhóm Comment (bình luận)

### `comment`
Gộp cả comment gốc lẫn reply vào 1 bảng tự tham chiếu — thay cho 2 khái niệm Mongo cũ: `Comment` document + mảng embedded `replies: List<ReplyComment>` bên trong nó.

| Field | Nhiệm vụ |
|---|---|
| `id` (PK) | UUID |
| `post_id` (FK post) | Post chứa comment (kể cả reply cũng ghi lại `post_id` để query nhanh, tránh phải join ngược qua `parent`) |
| `parent_id` (FK comment, tự tham chiếu, nullable) | `NULL` = comment gốc; khác `NULL` = reply của comment đó |
| `user_id` (FK account) | Người bình luận |
| `text` / `html` | Nội dung |
| `create_datetime` | Thời điểm bình luận, dùng sort theo thời gian |

Index: `(post_id) WHERE parent_id IS NULL` (lấy comment gốc của 1 post, dùng partial index vì đây là truy vấn phổ biến nhất),
`post_id` (index đầy đủ cho các truy vấn khác), `parent_id` (lấy reply của 1 comment).

Quan hệ: tự tham chiếu 1-N (`parent` → `replies`); N `comment` — N `comment_media`, N `comment_like`.
Xóa comment gốc → cascade xóa toàn bộ reply con (`ON DELETE CASCADE` trên `parent_id`) + media + like của cả comment lẫn reply.
Field `Comment.reply` (Boolean) của thiết kế cũ bị bỏ hẳn — entity Java suy ra qua `@Transient boolean isReply() { return parent != null; }`, không lưu cột dư thừa.

**Lưu ý riêng khi xóa post:** `comment.post_id` là cột thường (copy giá trị), *không* phải bị ràng buộc xóa qua FK tới `post` trong thiết kế Java hiện tại của phần service (dù SQL có khai báo FK `ON DELETE CASCADE`) — việc dọn comment khi xóa post còn được `CommentConsumer` xử lý qua sự kiện RabbitMQ `post.delete.exchange` để đồng bộ với hành vi cũ.

### `comment_media`
Ảnh/video đính kèm 1 comment/reply — thay cho `Content.mediaItems` embedded bên trong `Comment`/`ReplyComment` cũ.

| Field | Nhiệm vụ |
|---|---|
| `id` (PK) | UUID |
| `comment_id` (FK comment) | |
| `ord` | Thứ tự hiển thị |
| `url` / `type` / `width` / `height` | Giống `post_media` |

Index `(comment_id, ord)`.

### `comment_like`
Like cho comment/reply — thay cho `likes: List<String>` embedded trong `Comment`/`ReplyComment` cũ (trước đây update bằng positional operator `replies.$.likes`).

| Field | Nhiệm vụ |
|---|---|
| `comment_id` (PK, FK comment) | |
| `user_id` (PK, FK account) | |
| `created_at` | |

PK kép đảm bảo idempotent — không cần `elemMatch`/`$addToSet` như bản Mongo.
Index `user_id`.

---

## 4. Nhóm Notification (thông báo)

### `notification`
Thông báo gửi tới người dùng (like, comment, follow, ...). `EntityRef` (polymorphic reference tới post/comment gốc gây ra thông báo) được nhúng phẳng vào chính bảng này (JPA `@Embedded`), không tách bảng vì luôn đọc kèm.

| Field | Nhiệm vụ |
|---|---|
| `id` (PK) | UUID |
| `recipient_id` | Người nhận thông báo (không FK cứng — notification service tách biệt vòng đời khỏi account) |
| `sender_id` | Người gây ra hành động (like/comment/follow...); dùng để JOIN lấy displayName/avatar khi hiển thị, không lưu snapshot |
| `type` | Loại thông báo (enum `NotificationType`: LIKE, COMMENT, FOLLOW, ...) |
| `entity_type` / `entity_id` | Từ `EntityRef` — loại + id đối tượng gốc (post/comment) |
| `entity_preview` | Từ `EntityRef` — đoạn trích ngắn nội dung (VD: 50 ký tự đầu bài viết) |
| `entity_thumbnail_url` | Từ `EntityRef` — ảnh đại diện đính kèm thông báo |
| `group_key` | Khóa gộp nhóm (VD: nhiều người cùng like 1 post trong khoảng thời gian → gộp 1 thông báo) |
| `title` / `body` | Nội dung hiển thị, render sẵn từ template |
| `is_read` | Đã đọc chưa |
| `read_at` | Thời điểm đọc |
| `pushed` | Đã đẩy qua FCM push chưa (tránh push trùng) |
| `created_at` / `updated_at` | Kiểu `Instant` (khác các bảng khác dùng `LocalDateTime`) |

Index: `(recipient_id, is_read, created_at DESC)` (đếm/lọc chưa đọc), `(recipient_id, created_at DESC, id DESC)`
(phân trang kiểu keyset/cursor — vì UUID ngẫu nhiên không tăng dần theo thời gian như ObjectId Mongo cũ,
nên cursor phải so cả `created_at` lẫn `id`), `(recipient_id, group_key, created_at DESC)` (gộp nhóm).

Quan hệ: N `notification_metadata`, N `notification_sender` (con của 1 notification).

### `notification_metadata`
Payload tự do (deep link, dữ liệu custom FCM, url ảnh...) — thay cho `Notification.metadata: Map<String,Object>` embedded.

| Field | Nhiệm vụ |
|---|---|
| `notification_id` (PK, FK notification) | |
| `meta_key` (PK) | Tên field |
| `meta_value` | Giá trị (lưu dạng text, ép kiểu ở tầng app) |

### `notification_sender`
Danh sách người gây ra hành động khi 1 thông báo bị gộp nhóm (VD: "A, B và 3 người khác đã thích bài viết") — thay cho `Notification.aggregatedSenderIds: List<String>` embedded.

| Field | Nhiệm vụ |
|---|---|
| `notification_id` (PK, FK notification) | |
| `sender_id` | Id người dùng |
| `ord` (PK cùng notification_id) | Thứ tự thêm vào — mảng có thứ tự nên PK phải gồm cả `ord`, không chỉ `sender_id` |

### `notification_preference`
Cấu hình nhận thông báo của 1 user (theo từng loại + quiet hours).

| Field | Nhiệm vụ |
|---|---|
| `id` (PK) | UUID |
| `user_id` (UNIQUE) | 1-1 với account |
| `quiet_hours_start` / `quiet_hours_end` | Khung giờ im lặng (VD "22:00"–"07:00") |
| `timezone` | Múi giờ áp dụng quiet hours (VD "Asia/Ho_Chi_Minh") |

Quan hệ: 1 preference — N `preference_setting` (mỗi loại thông báo 1 dòng cấu hình kênh).

### `preference_setting`
Bật/tắt kênh nhận (push/email/in-app) theo từng `NotificationType` — thay cho `NotificationPreference.settings: Map<String, ChannelSettings>` embedded.

| Field | Nhiệm vụ |
|---|---|
| `preference_id` (PK, FK notification_preference) | |
| `notification_type` (PK) | Loại thông báo áp dụng (LIKE, COMMENT, ...) |
| `push_enabled` / `email_enabled` / `in_app_enabled` | Bật/tắt từng kênh |

### `notification_template`
Template dựng sẵn nội dung thông báo theo loại (tránh hard-code chuỗi trong code).

| Field | Nhiệm vụ |
|---|---|
| `id` (PK) | UUID |
| `type` (UNIQUE) | 1 loại thông báo — 1 template |
| `is_active` | Bật/tắt template |

Quan hệ: 1 template — N `template_translation` (đa ngôn ngữ), N `template_default_data`.

### `template_translation`
Nội dung template theo từng ngôn ngữ — thay cho `NotificationTemplate.translations: Map<String, LocalizedTemplate>` embedded.

| Field | Nhiệm vụ |
|---|---|
| `template_id` (PK, FK notification_template) | |
| `locale` (PK) | "vi", "en", "ja"... |
| `title_template` / `body_template` | Chuỗi có placeholder (VD: `"{{actor}} đã thích bài viết của bạn"`) |

### `template_default_data`
Dữ liệu mặc định điền vào placeholder khi không có giá trị cụ thể — thay cho `NotificationTemplate.defaultData: Map<String,String>` embedded.

| Field | Nhiệm vụ |
|---|---|
| `template_id` (PK, FK notification_template) | |
| `data_key` (PK) | |
| `data_value` | |

### `device_token`
Token thiết bị để bắn push notification qua Firebase (FCM).

| Field | Nhiệm vụ |
|---|---|
| `id` (PK) | UUID |
| `user_id` | Chủ thiết bị |
| `fcm_token` (UNIQUE) | Token FCM, unique toàn hệ thống (1 token vật lý chỉ gắn 1 record) |
| `device_id` | Định danh thiết bị (app tự sinh) |
| `device_type` / `device_name` / `app_version` / `os_version` | Thông tin thiết bị, phục vụ debug/segment push |
| `is_active` | Token còn hiệu lực không (FCM báo lỗi → set false thay vì xóa ngay) |
| `last_use_at` | Lần cuối dùng — dọn token cũ |

Ràng buộc `UNIQUE(user_id, device_id)` — 1 user trên 1 thiết bị chỉ có 1 token hiện hành (token mới ghi đè token cũ cùng thiết bị).
Index `(user_id, is_active)` — lấy nhanh danh sách token đang hoạt động để bắn push.

---

## 5. Nhóm Feed / Interest Graph (thuật toán gợi ý)

### `user_interest`
Trọng số quan tâm của user theo từng tag — thay cho `UserInterests.interests: List<InterestItem>` embedded (trước đây phải upsert rồi tăng theo vị trí phần tử trong mảng — pattern dễ lỗi).

| Field | Nhiệm vụ |
|---|---|
| `user_id` (PK, FK account) | |
| `tag` (PK) | |
| `weight` | Trọng số quan tâm — tăng dần khi user tương tác với bài có tag đó |
| `updated_at` | Lần cập nhật gần nhất — dùng cho decay theo thời gian |

Tăng trọng số = `INSERT ... ON CONFLICT (user_id, tag) DO UPDATE SET weight = weight + :delta` (1 câu, không cần đọc-rồi-ghi).
Chỉ giữ top-10 tag/user (dọn bớt tag ít quan tâm để tránh loãng affinity) và áp decay định kỳ (`weight *= factor`, xóa khi dưới ngưỡng) — coi `InterestGraphServiceImpl`.
Index `(user_id, weight DESC)` (lấy top tag của 1 user), `tag` (tìm user quan tâm 1 tag).

### `seen_post`
Đánh dấu bài user đã xem — dùng để loại bài trùng khi load feed tiếp theo.

| Field | Nhiệm vụ |
|---|---|
| `user_id` (PK, FK account) | |
| `post_id` (PK, FK post) | |
| `seen_at` | Thời điểm xem |

Postgres không có TTL index như Mongo — dữ liệu quá 14 ngày bị dọn bằng job `SeenPostPurgeScheduler` (chạy 3h sáng hằng ngày) thay vì tự động hết hạn.
Index `seen_at` — phục vụ query dọn theo ngưỡng thời gian.
Xóa post → cascade xóa dòng seen_post liên quan.

### `tag_cooccurrence`
Đếm số lần 2 tag xuất hiện cùng nhau trên 1 post — phục vụ phần Explore (20%) của feed: từ tag user đang quan tâm, tìm tag liên quan để gợi ý nội dung mới.

| Field | Nhiệm vụ |
|---|---|
| `tag_a` (PK) | |
| `tag_b` (PK) | |
| `count` | Số lần cùng xuất hiện |

Index `(tag_a, count DESC)` — lấy tag liên quan nhiều nhất với 1 tag cho trước.

---

## 6. Nhóm Complaint / Attachment / Term / Email

### `complaint`
Báo cáo vi phạm (report) nhắm vào 1 đối tượng (post/comment/account).

| Field | Nhiệm vụ |
|---|---|
| `id` (PK) | UUID |
| `target_id` | Id đối tượng bị báo cáo (không FK cứng vì có thể trỏ tới nhiều loại bảng khác nhau — polymorphic) |
| `complaint_type` | Loại đối tượng bị báo cáo (enum `ComplaintType`) |
| `is_read` | Admin đã xem báo cáo này chưa |

Quan hệ: 1 complaint — N `complaint_detail` (nhiều người cùng report 1 đối tượng, mỗi lần report ghi 1 dòng detail — thay cho `Complaint.details: List<ComplaintDetail>` embedded).
Index `target_id` — tra cứu tất cả report nhắm vào 1 đối tượng.

### `complaint_detail`
Từng lượt report cụ thể trong 1 complaint.

| Field | Nhiệm vụ |
|---|---|
| `id` (PK) | UUID |
| `complaint_id` (FK complaint) | |
| `user_id` | Người report |
| `term_of_service_id` (FK term_of_service, nullable) | Điều khoản bị vi phạm được người dùng chọn khi report; `NULL` nếu xóa term (`ON DELETE SET NULL`) |
| `create_datetime` | Thời điểm report — dùng thống kê complaint theo ngày |

Index `complaint_id`, `create_datetime`.

### `term_of_service`
Danh mục điều khoản dịch vụ dùng làm lý do report (VD: "Nội dung bạo lực", "Spam"...).

| Field | Nhiệm vụ |
|---|---|
| `id` (PK) | UUID |
| `name` | Tên điều khoản |
| `status` | Còn áp dụng hay đã ẩn |

### `attachments`
File đính kèm chung (không nhất thiết gắn với post — VD ảnh trong quá trình soạn thảo, ảnh profile...). Độc lập với `post_media`/`comment_media` vì vòng đời khác (không cascade theo post/comment).

| Field | Nhiệm vụ |
|---|---|
| `id` (PK) | UUID |
| `public_id` | Public ID trên Cloudinary — dùng để xóa file gốc khi cần |
| `resource_type` / `file_type` | Loại tài nguyên Cloudinary / loại file thực tế |
| `size` | Kích thước file |
| `url` | URL truy cập |
| `owner_id` | Người upload |

Index `owner_id`.

### `email_template`
Mẫu email hệ thống gửi (OTP, thông báo, chào mừng...).

| Field | Nhiệm vụ |
|---|---|
| `id` (PK) | UUID |
| `name` | Tên định danh template |
| `content` | Nội dung HTML email |
| `is_active` | Đang dùng hay không |
| `is_default` | Template mặc định khi không tìm thấy template cụ thể |

### `email_template_field`
Danh sách các biến placeholder có thể dùng trong `email_template.content` (VD: `{{otp}}`, `{{username}}`) — bảng tra cứu/tài liệu hóa, không FK trực tiếp tới `email_template` (dùng chung cho mọi template).

| Field | Nhiệm vụ |
|---|---|
| `id` (PK) | UUID |
| `name` | Tên biến |
| `description` | Mô tả ý nghĩa biến |

---

## Sơ đồ quan hệ tổng quát (rút gọn)

```
role ──┬── role_permission ──── permission
       │
account ─┬── token (1-1)
         ├── refresh_token (theo username)
         ├── follow (follower_id / followee_id, tự tham chiếu N-N)
         ├── post (owner_id) ─┬── post_media
         │                    ├── post_like ── account (user_id)
         │                    ├── post_tag
         │                    ├── comment (post_id) ─┬── comment (parent_id, tự tham chiếu = reply)
         │                    │                      ├── comment_media
         │                    │                      └── comment_like ── account (user_id)
         │                    └── seen_post ── account (user_id)
         ├── user_interest (user_id, tag)
         ├── notification (recipient_id / sender_id) ─┬── notification_metadata
         │                                             └── notification_sender
         ├── notification_preference (1-1) ── preference_setting
         ├── device_token
         ├── complaint (target_id, polymorphic) ── complaint_detail ── term_of_service
         └── attachments (owner_id)

notification_template ─┬── template_translation
                        └── template_default_data

tag_cooccurrence (tag_a, tag_b — độc lập, không FK tới post_tag)
email_template / email_template_field (độc lập)
```

---

## Ghi chú thiết kế chung

- **Không dùng embedded document/mảng/Map** như bản Mongo — mọi collection lồng nhau được tách thành bảng con có FK, đảm bảo toàn vẹn dữ liệu bằng ràng buộc DB thay vì code tầng application.
- **Composite key (`@IdClass`)** dùng cho mọi bảng join thuần túy (`post_like`, `post_tag`, `comment_like`, `follow`, `user_interest`, `seen_post`, `tag_cooccurrence`) — tránh phát sinh UUID vô nghĩa cho quan hệ chỉ cần 2 khóa ngoại.
- **`ON DELETE CASCADE`** áp dụng xuyên suốt cho quan hệ cha-con thật sự (post → media/like/tag; comment → reply/media/like; complaint → detail) để xóa 1 dòng cha tự dọn sạch dữ liệu con, không cần code dọn thủ công.
- **Không snapshot dữ liệu người dùng** (tên, avatar) vào bảng khác — luôn JOIN `account` để lấy dữ liệu mới nhất, tránh lệch dữ liệu khi user đổi tên/avatar (khác với `ActorSnapshot` embedded của bản Mongo).
- **`ddl-auto: validate`** là cổng kiểm tra chính — entity Java phải khớp tuyệt đối schema Flyway, không cho Hibernate tự sinh/sửa bảng ở runtime.
