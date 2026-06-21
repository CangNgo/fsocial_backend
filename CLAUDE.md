# postService — CLAUDE.md

## Overview
Spring Boot 3.4.1 microservice (Java 21) trong hệ thống mạng xã hội Fsocial.  
Chịu trách nhiệm: bài viết (post), comment, reply, like, share, notification, authentication.  
Base URL: `http://localhost:8080/api/v1/post`

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.4.1, Java 21 |
| Database | MongoDB Atlas (spring-data-mongodb) |
| Cache | Redis + Redisson (distributed lock) |
| Message Queue | RabbitMQ (CloudAMQP, SSL port 5671) |
| Security | Spring Security + OAuth2 Resource Server + JWT (jjwt 0.12.6) |
| Mapping | MapStruct 1.6.3 |
| Push Notification | Firebase Admin SDK 9.4.3 (FCM) |
| File Upload | Cloudinary 2.0.0 |
| Email | SendGrid 4.10.2 + Spring Mail (SMTP Gmail) |
| Docs | SpringDoc OpenAPI 2.8.5 (Swagger UI) |
| Utilities | Lombok, java-dotenv, Jackson |

---

## Directory Structure

```
src/main/java/com/fsocial/postservice/
├── PostServiceApplication.java       # @SpringBootApplication, @EnableScheduling
├── config/                            # 14 config classes
│   ├── AppConfig.java                 # SecurityFilterChain, JWT oauth2ResourceServer
│   ├── RabbitMQConfig.java            # Queues, FanoutExchange, MessageConverter
│   ├── RedisConfig.java               # Lettuce connection, RedisTemplate beans
│   ├── FirebaseConfig.java            # FirebaseApp + FirebaseMessaging bean
│   ├── CorsConfig.java                # Allowed origins (localhost, fsocial.online)
│   ├── CustomJwtDecode.java           # Nimbus SignedJWT parser
│   └── ConfigCloudinary.java          # Cloudinary API setup
├── controller/                        # 15 controllers (REST endpoints)
├── comsumer/                          # RabbitMQ listeners (Comment, Attachments)
├── publisher/                         # PostEventPublisher (RabbitMQ fanout)
├── services/                          # Service interfaces
│   └── impl/                          # Service implementations (25+)
├── repository/                        # MongoRepository interfaces (20+)
├── entity/                            # MongoDB @Document entities (30+)
├── dto/                               # DTOs organized by domain (50+)
│   ├── post/, comment/, replyComment/
│   ├── notification/, request/, response/
│   └── Account/, complaint/, ...
├── mapper/                            # MapStruct mappers (9 files)
├── enums/                             # Enums: NotificationType, TopicKafka, EntityType...
├── exception/                         # GlobalExceptionHandler + StatusCode enum
├── scheduler/                         # @Scheduled tasks
└── util/                              # CloudinaryUtils, helper methods

src/main/resources/
├── application.yml                    # Profile selector
├── application-dev.yml                # Dev config (port 8080, local Redis/MongoDB)
├── application-pro.yml                # Production config
└── firebase/firebase-service-account.json
```

---

## Key Entities (MongoDB Collections)

| Entity | Collection | Notes |
|---|---|---|
| `Post` | post | owner (embedded), content (embedded), likes (List<userId>) |
| `Comment` | comment | postId, content (embedded), likes |
| `ReplyComment` | reply_comment | commentId reference |
| `Account` | account | username unique, BCrypt password, role (DBRef) |
| `Notification` | notification | aggregatedActors, groupKey, compound indexes |
| `DeviceToken` | device_token | FCM token, unique (userId, deviceId) |
| `Relationship` | relationship | listFollower/listFollowing (Set<String>) |
| `RefreshToken` | refresh_token | token blacklist management |

**Base entity:** `AbstractEntity` — id (UUID String), createdBy, updatedBy, createdAt, updatedAt.

---

## API Response Wrapper

Tất cả endpoints trả về:
```json
{
  "statusCode": 200,
  "message": "...",
  "dateTime": "yyyy-MM-dd HH:mm:ss",
  "data": { ... }
}
```
Class: `Response<T>` (generic) và `ApiResponse<T>`.

---

## Security

- **Public endpoints:** `/auth/**`, `/account/register`, `/account/send-otp`, `/account/verify-otp`, `/account/check-duplication`, `/account/reset-password`, `/internal/**`, `/docs`, `/swagger-ui/**`, `/notification/**`
- **Protected:** tất cả endpoints còn lại yêu cầu JWT Bearer token
- **Roles:** `ROLE_USER`, `ROLE_ADMIN` — extract từ JWT claims
- **JWT:** `CustomJwtDecode` parse Nimbus `SignedJWT`, signer key 64-char hex, duration 60 phút, refresh token 7 ngày

---

## Typical API Flow — Create Post

```
POST /api/v1/post/actions  (multipart/form-data, Authorization: Bearer <JWT>)

1. PostController.createPost()
   - @PreAuthorize("hasRole('USER')")
   - Extract userId from JWT subject
   - Validate text OR media exists
   - postService.createPost(request)

2. PostServiceImpl.createPost()
   - Upload media → CloudinaryUtils → returns URL[]
   - accountService.getOwner(userId) → fetch displayName, avatar
   - Build Post entity, set owner, content, createDatetime
   - postRepository.save(post)  [MongoDB]
   - redisService.personalization(userId, ...)  [Redis cache]
   - Map Post → PostDTO via PostMapper

3. PostRepository (MongoRepository)
   - .save() → MongoDB "post" collection

4. Response: { statusCode: 201, message: "Tạo bài viết thành công", data: PostDTO }
```

---

## Redis Cache Keys

| Key Pattern | Purpose |
|---|---|
| `viewed:{userId}` | Danh sách postId đã xem (feed deduplication) |
| `personalization:{userId}` | Posts ưu tiên hiện lên đầu feed |
| `viewedFollowing:{userId}` | Following feed deduplication |

---

## RabbitMQ Events

| Exchange | Type | Queue | Consumer |
|---|---|---|---|
| `post.delete.exchange` | Fanout | `post.comment.delete.queue` | `CommentConsumer` → xóa comments theo postId |
| `post.delete.exchange` | Fanout | `post.attachments.delete.queue` | `AttachmentsConsumer` → xóa attachments |

**Publish:** `PostEventPublisher.eventDeletePost(postId)` — gửi khi xóa post.

---

## Exception Handling

- `GlobalExceptionHandler` (@ControllerAdvice) bắt tất cả exceptions
- `AppCheckedException` / `AppUnCheckedException` — custom exceptions với `StatusCode` enum
- `StatusCode` enum: code number + message (`CREATE_POST_SUCCESS=201`, `USER_NOT_FOUND=208`, ...)
- 403 → `AccessDeniedException`, 404 → `NoResourceFoundException`, validation → `MethodArgumentNotValidException`

---

## Service-to-Service Communication

- **Profile Service:** `http://localhost:8085` — `ProfileClient` (RestTemplate)
- **Notification Service:** `http://localhost:8086`
- **Internal APIs:** `/internal/**` — không cần JWT, dùng cho service-to-service call

---

## Naming Conventions

- Controllers: `XxxController.java` → endpoint path `/xxx`
- Services: interface `XxxService` + impl `XxxServiceImpl`
- DTOs: `XxxDTO` (response), `XxxDTORequest` (input), `XxxResponse` (complex response)
- Mappers: `XxxMapper` với `@Mapper(componentModel = "spring")`
- MongoDB queries: custom `@Query` hoặc `@Aggregation` trong Repository interface

---

## Profiles & Configs

- Active profile set qua env `SPRING_PROFILES_ACTIVE=dev|pro`
- Dev: MongoDB Atlas cluster0, Redis localhost:6379, RabbitMQ CloudAMQP
- File upload limit: 50MB per file, 100MB per request
- Swagger UI: `/api/v1/post/swagger-ui/index.html`
