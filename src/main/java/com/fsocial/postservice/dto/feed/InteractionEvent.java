package com.fsocial.postservice.dto.feed;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Event published to RabbitMQ when a user interacts with a post.
 * Consumed by ScoreUpdateConsumer and InterestUpdateConsumer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InteractionEvent implements Serializable {

    String postId;
    String userId;
    String actionType; // LIKE, UNLIKE, COMMENT, SHARE
    List<String> postTags;
    LocalDateTime timestamp;
}
