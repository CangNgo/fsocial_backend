package com.fsocial.postservice.dto.post;

import com.fsocial.postservice.entity.Content;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostResponse {
    String id;
    String userId;
    String originPostId;
    Content content;
    Integer countLikes;
    Integer countComments;
    String displayName;
    String avatar;
    LocalDateTime createDatetime;
    boolean isShare;
    boolean isLike;
    boolean status;
    List<String> tags;
}