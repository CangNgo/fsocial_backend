package com.fsocial.dto.comment;

import com.fsocial.dto.ContentDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    String id;
    String postId;
    String userId;
    ContentDTO content;
    int countLikes;
    int countReplyComment;
    boolean reply;
}
