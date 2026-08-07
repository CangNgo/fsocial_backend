package com.fsocial.dto.comment;

import com.fsocial.dto.post.ContentResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentResponse {
    String id;
    String postId;
    String userId;
    ContentResponse content;
    Integer countLikes;
    Integer countReplies;
    String displayName;
    String avatar;
    LocalDateTime createDatetime;
    boolean reply;
    boolean like;
}
