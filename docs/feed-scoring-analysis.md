# Phân tích luồng Get bài viết & Tính điểm (Feed Scoring)

> Cập nhật: 2026-07-11. Nguồn: `PostController`, `PostServiceImpl`, `FeedServiceImpl`, `ScoringServiceImpl`, `InterestGraphServiceImpl`, `ScoreUpdateConsumer`, `InterestUpdateConsumer`, `InterestDecayScheduler`, `PostRepository`.

---

## 1. Các luồng GET bài viết

| Endpoint | Method service | Cơ chế |
|---|---|---|
| `GET /actions?userId=` | `getPostsByUserId` → `feedService.buildPersonalizedFeed(userId, 10)` | Feed cá nhân hóa (scoring) |
| `GET /actions/following?userId=` | `getPostByFollowing` | Chronological từ following, dedup bằng Redis `viewedFollowing:{userId}` |
| `GET /actions/user/{userId}?requesterId=` | `getPostsByUser` | Toàn bộ post của 1 user, check privacy (public / follower) |
| `GET /actions/find` | `findByText` | Regex contains ignore-case trên `content.text` |
| `GET /actions/getpost_id` | `getPostById` | 1 post theo id |

## 2. Luồng feed cá nhân hóa (`buildPersonalizedFeed`)

```
GET /actions?userId=X  (feedSize = 10, hardcode)
│
├─ 1. interestGraphService.getNormalizedWeights(userId)
│     user_interests.interests[{tag, weight}] → weight/Σweight (chuẩn hóa về tổng = 1)
│
├─ 2. seenPostRepository.findByUserId → danh sách postId đã xem (Mongo, collection seen_post)
│
├─ 3. Build candidate pool
│     ├─ Cold start (weights rỗng): chronological, loại seen
│     ├─ Exploit 70%: mỗi tag được cấp slot ≈ weight × totalSlots (min 1),
│     │   query findByTagAndIdNotIn sort global_score desc, pool = slots×5 (max 100),
│     │   rồi shuffle lấy ngẫu nhiên `slots` bài
│     ├─ Explore 20%: tag liên quan qua tag_cooccurrence (top 5/tag),
│     │   loại tag đã biết → findByTagsInAndIdNotIn sort global_score desc
│     └─ Wildcard 10%: findTopByGlobalScore (top toàn cục chưa xem)
│
├─ 4. Nếu pool rỗng → xóa toàn bộ seen_post của user, trả chronological mới
│
├─ 5. Sort candidates theo final_score (tính on-the-fly, xem §3)
│
├─ 6. markSeen từng bài (upsert seen_post)
│
└─ 7. toPostResponses: batch load Account + comment count → PostResponse
```

## 3. Công thức tính điểm

### 3.1 Global score (lưu vào `post.global_score`)

```
global_score = max(0, likes×2 + comments×3 + shares×5 − ln(age_hours + 1)×10)
```

- Tính lại **bất đồng bộ** bởi `ScoreUpdateConsumer` khi có `InteractionEvent` (LIKE/UNLIKE/SHARE/COMMENT) qua RabbitMQ.
- `SHARE` còn `inc share_count` trên post gốc.

### 3.2 Personal affinity (tính lúc build feed, không lưu)

```
affinity = min(1.0, Σ normalized_weight[tag] với tag ∈ post.tags)
affinity = 0.5 nếu user chưa có interests hoặc post không có tags (cold start)
```

### 3.3 Final score (chỉ dùng để sort trong 1 request)

```
final_score = global_score × affinity × social_boost
social_boost = 1.5 nếu requester follow tác giả, ngược lại 1.0
```

### 3.4 Interest graph (`user_interests`)

- `InterestUpdateConsumer` cập nhật weight theo action: LIKE +2, UNLIKE −2, COMMENT +3, SHARE +5 (cộng cho **mọi tag** của post).
- `InterestDecayScheduler` chạy 2:00 AM hằng ngày: `weight ×= 0.95`, xóa entry < 0.1.

---

## 4. Nhận xét

### Điểm tốt
- Kiến trúc exploit/explore/wildcard 70/20/10 hợp lý, tránh filter bubble.
- Score update bất đồng bộ qua MQ — request write không bị chậm.
- Có compound index `{tags: 1, global_score: -1}` đúng với query pattern.
- Batch load account + comment count trong `toPostResponses`, tránh N+1.

### Vấn đề

1. ✅ ĐÃ FIX **Bảo mật — nghiêm trọng nhất**: `GET /actions?userId=` nhận `userId` từ query param thay vì JWT subject (`PostServiceImpl.java:219`, `PostController.java:128`). Bất kỳ ai có token đều xem được feed/seen-state của user khác, và `markSeen` ghi bẩn dữ liệu của họ. Tương tự `likePost` nhận `userId` trong body. `createPost` đã làm đúng (lấy từ `jwt.getSubject()`).

2. ✅ ĐÃ FIX **`global_score` bị stale theo thời gian**: time penalty chỉ được tính lại khi có interaction mới. Post cũ không ai tương tác giữ nguyên điểm cao mãi → wildcard/exploit query (sort theo `global_score` trong DB) trả về bài cũ. Final score có tính lại penalty lúc sort, nhưng **candidate selection** thì không.

3. ✅ ĐÃ FIX **Double scoring không nhất quán**: interaction vừa cộng vào `global_score` (×2/×3/×5) vừa cộng vào interest weight (+2/+3/+5) — chấp nhận được, nhưng UNLIKE chỉ trừ interest, `global_score` được recompute từ likes list nên OK; riêng **COMMENT delete** không thấy event trừ điểm.

4. ✅ ĐÃ FIX **`weightedSample` không weighted**: comment nói "weighted sampling" nhưng thực tế `Collections.shuffle` — uniform random (`FeedServiceImpl.java:183`). Pool đã sort theo `global_score` rồi shuffle làm mất luôn thứ tự đó.

5. ✅ ĐÃ FIX **Seen list phình vô hạn trong request**: `$nin: seenIds` — user hoạt động lâu có hàng nghìn seen id, query Mongo với mảng `$nin` lớn rất chậm và không dùng được index hiệu quả. Không có TTL cho `seen_post` (chỉ reset khi pool cạn).

6. ✅ ĐÃ FIX **Sort dùng comparator gọi `calculateFinalScore` O(n log n) lần**: mỗi lần so sánh tính lại score 2 bài (ln, stream sum). Với n=10 không sao, nhưng đúng ra tính 1 lần/bài rồi sort.

7. ✅ ĐÃ FIX **`applyDecay` load toàn bộ collection vào memory** (`findAll()` rồi save từng doc) — không scale khi user nhiều.

8. ✅ ĐÃ FIX **`getNormalizedWeights` chia đều tổng**: user có 50 tag thì mỗi tag weight rất nhỏ → affinity thấp toàn bộ → final score co cụm, phân biệt kém; trong khi user 1 tag luôn có affinity ≈ 1.

9. ✅ ĐÃ FIX **Feed size hardcode = 10, không phân trang** — client không thể load-more theo cursor; "phân trang" thực chất là gọi lại và dựa vào seen-dedup.

10. ✅ ĐÃ FIX **`updateInterests` có race**: check-then-push (`matchedCount == 0` → `upsert`) không atomic; 2 event song song cùng tag mới có thể push trùng entry `interests.tag`.

---

## 5. Đề xuất cải tiến (theo độ ưu tiên)

### P0 — Bảo mật
Lấy `userId` từ JWT trong mọi endpoint feed/like/share, bỏ nhận từ client:

```java
@GetMapping
public ApiResponse<List<PostResponse>> getPosts(@AuthenticationPrincipal Jwt jwt) {
    return ... postService.getPostsByUserId(jwt.getSubject()) ...;
}
```

### P1 — Sửa score stale, không cần job recompute
Tách time penalty ra khỏi giá trị lưu trữ. Lưu `raw_engagement = likes×2 + comments×3 + shares×5` (không trừ penalty), còn penalty/decay tính lúc đọc. Với candidate query, thêm điều kiện thời gian (vd chỉ lấy post ≤ 7 ngày cho exploit/wildcard) hoặc dùng công thức kiểu Reddit hot: `score = raw / (age_hours + 2)^1.5` — precompute được bằng cách sort theo `(raw_engagement, createDatetime)` hoặc chạy 1 scheduled job nhẹ recompute score cho post active trong N ngày.

### P1 — Seen list
- Thêm TTL index cho `seen_post.seen_at` (vd 7–14 ngày) — Mongo tự dọn, `$nin` luôn bounded.
- Hoặc chuyển sang Redis Bloom filter / SET có TTL (repo đã có Redis + pattern `viewed:{userId}` sẵn).

### P2 — Sort & sampling
```java
// tính score 1 lần rồi sort
record Scored(Post post, double score) {}
candidates.stream()
    .map(p -> new Scored(p, scoringService.calculateFinalScore(...)))
    .sorted(Comparator.comparingDouble(Scored::score).reversed())
```
Và làm `weightedSample` đúng nghĩa (sample xác suất tỉ lệ `global_score`) hoặc đổi tên/comment cho khớp thực tế.

### P2 — Normalization
Thay chia-tổng bằng softmax hoặc chỉ normalize trên top-K tag (vd top 10 theo weight) để affinity không bị pha loãng khi user có nhiều interest.

### P2 — Decay job
Thay `findAll()` + save-per-doc bằng 1 lệnh bulk:
```js
db.user_interests.updateMany({}, [
  { $set: { interests: { $filter: {
      input: { $map: { input: "$interests", in: { tag: "$$this.tag",
               weight: { $multiply: ["$$this.weight", 0.95] }, updated_at: "$$NOW" } } },
      cond: { $gte: ["$$this.weight", 0.1] } } } } }
])
```
(aggregation pipeline update — 1 round-trip, chạy server-side).

### P3 — Phân trang
Nhận `feedSize`/`cursor` từ request thay vì hardcode 10; hoặc giữ seen-based nhưng document hóa rõ contract cho FE.

### P3 — Interest update race
Dùng `$addToSet`-style: upsert document trước (`setOnInsert`), rồi update array bằng arrayFilters, hoặc chuyển `interests` thành sub-document map `{tag: weight}` để `inc("interests.<tag>", delta)` atomic hoàn toàn.

### P3 — COMMENT delete
Publish event khi xóa comment để recompute `global_score` (hiện chỉ có create-side).

---

## 6. Đã fix (2026-07-11)

### P0 — Bảo mật: userId từ JWT
**File:** `PostController.java`
- `GET /actions`, `GET /actions/following`, `GET /actions/find`, `GET /actions/getpost_id`, `GET /actions/user/{userId}`: bỏ `@RequestParam userId`, thêm `@AuthenticationPrincipal Jwt jwt`, lấy `userId = jwt.getSubject()`.
- `POST /actions/like`: override `userId = jwt.getSubject()` thay vì dùng `likeDTO.getUserId()` (giữ DTO để không vỡ FE).
- `POST /actions/share`: set `share.setUserId(jwt.getSubject())` trước khi gọi service.

### P1 — Score stale: thêm raw_engagement + candidate date filter
**Files:** `Post.java`, `ScoringService.java`, `ScoringServiceImpl.java`, `ScoreUpdateConsumer.java`, `PostRepository.java`, `FeedServiceImpl.java`
- `Post.java`: thêm field `rawEngagement` (`@Field("raw_engagement")`, double, `@Builder.Default 0.0`). Giữ `globalScore` tương thích.
- `ScoringService/Impl`: thêm `calculateRawEngagement` = likes×2 + comments×3 + shares×5. `calculateGlobalScore` = `max(0, rawEngagement - timePenalty)`.
- `ScoreUpdateConsumer`: set cả `raw_engagement` và `global_score` khi recompute.
- `PostRepository`: thêm 3 query mới (`findByTagAndIdNotInSince`, `findByTagsInAndIdNotInSince`, `findTopByGlobalScoreSince`) với điều kiện `created_datetime >= since`. Giữ query cũ làm fallback.
- `FeedServiceImpl`: hằng số `CANDIDATE_MAX_AGE_DAYS = 7`. Các method `getExploitPosts`, `getExplorePosts`, `getWildcardPosts` thử query date-bounded trước, fallback sang unbounded nếu rỗng.

### P1 — Seen list TTL 14 ngày
**File:** `MongoIndexInitializer.java`
- TTL index đã được tạo programmatically qua `ApplicationRunner`. Đổi từ 30 ngày → 14 ngày. `auto-index-creation: false` trong yml — index tạo qua `MongoIndexInitializer` (cách ít xâm lấn, không bật auto-index để tránh overhead production).

### P2 — Sort 1 lần + weighted sample đúng
**File:** `FeedServiceImpl.java`
- Sort: dùng `record Scored(Post post, double score)` tính score 1 lần/post rồi sort desc.
- `weightedSample`: sampling không hoàn lại với xác suất tỉ lệ `globalScore` (min weight 0.1 để post 0 điểm vẫn có cơ hội). Dùng `ThreadLocalRandom`.

### P2 — Normalization top-K
**File:** `InterestGraphServiceImpl.java`
- `getNormalizedWeights`: chỉ lấy top 10 tag theo raw weight (`TOP_K_INTERESTS = 10`) rồi normalize trên tập đó — tránh affinity bị pha loãng khi user có nhiều interest.

### P2 — Decay bulk
**File:** `InterestGraphServiceImpl.java`
- `applyDecay`: thay `findAll()` + save-per-doc bằng 1 `updateMany` với aggregation pipeline Document qua `mongoTemplate.getCollection(...)`. 1 round-trip, xử lý server-side, không load data vào memory. Log số `modifiedCount`.

### P3 — Phân trang feedSize
**Files:** `PostController.java`, `PostService.java`, `PostServiceImpl.java`
- `GET /actions`: thêm `@RequestParam(value = "size", defaultValue = "10") int size`, clamp `[1, 50]`, truyền xuống `getPostsByUserId(userId, feedSize)`.
- `PostService`: thêm overload `getPostsByUserId(String userId, int feedSize)`.
- `PostServiceImpl`: implement overload, method cũ delegate với `feedSize = 10`.

### P3 — Interest update race
**File:** `InterestGraphServiceImpl.java`
- `updateInterests`: upsert document rỗng trước (`setOnInsert user_id + interests: []`), đảm bảo document tồn tại trước khi vào loop per-tag. Khi `push` matched 0 (concurrent thread đã push tag), retry `inc` positional 1 lần.

### P3 — COMMENT delete event
**Files:** `CommentServiceImpl.java`, `InterestUpdateConsumer.java`
- `CommentServiceImpl.deleteComment`: load comment trước khi xóa, sau đó publish `COMMENT_DELETE` event (postId, userId, tags của post).
- `InterestUpdateConsumer`: thêm case `COMMENT_DELETE` → delta = -3.0. `ScoreUpdateConsumer` đã recompute cho mọi event (không cần sửa thêm).
