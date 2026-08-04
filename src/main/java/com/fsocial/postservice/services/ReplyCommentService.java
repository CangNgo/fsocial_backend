package com.fsocial.postservice.services;

import com.fsocial.postservice.dto.replyComment.LikeReplyCommentDTO;
import com.fsocial.postservice.dto.replyComment.ReplyCommentRequest;
import com.fsocial.postservice.dto.replyComment.ReplyCommentResponse;
import com.fsocial.postservice.dto.replyComment.ReplyCommentUpdateDTORequest;

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
