package com.fsocial.services;

import com.fsocial.dto.replyComment.LikeReplyCommentDTO;
import com.fsocial.dto.replyComment.ReplyCommentRequest;
import com.fsocial.dto.replyComment.ReplyCommentResponse;
import com.fsocial.dto.replyComment.ReplyCommentUpdateDTORequest;

import java.util.List;

public interface ReplyCommentService {

    ReplyCommentResponse addReplyComment(ReplyCommentRequest request);

    ReplyCommentResponse updateReplyComment(ReplyCommentUpdateDTORequest request);

    String deleteReplyComment(String replyCommentId);

    // Methods from timelineService
    List<ReplyCommentResponse> getReplyCommentsByCommentId(String commentId);

    // Method for like reply comment
    boolean likeReplyComment(LikeReplyCommentDTO request);
}
