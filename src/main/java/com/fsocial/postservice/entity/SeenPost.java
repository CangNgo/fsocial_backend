package com.fsocial.postservice.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * Tracks which posts a user has already seen.
 * TTL index on seenAt causes MongoDB to auto-delete after 30 days.
 * Index creation is handled by MongoIndexInitializer on startup.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "seen_posts")
@CompoundIndexes({
    @CompoundIndex(name = "idx_userId_postId", def = "{'user_id': 1, 'post_id': 1}", unique = true)
})
public class SeenPost {

    @Id
    String id;

    @Field("user_id")
    @Indexed
    String userId;

    @Field("post_id")
    String postId;

    // TTL index created programmatically in MongoIndexInitializer (expireAfterSeconds = 2592000)
    @Field("seen_at")
    LocalDateTime seenAt;
}
