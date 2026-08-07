package com.fsocial.dto.replyComment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyCommentUpdateDTORequest {
    String replyCommentId;
    String userId;
    String text;
    String html;
}
