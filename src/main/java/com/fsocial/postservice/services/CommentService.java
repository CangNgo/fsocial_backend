package com.fsocial.postservice.services;

import com.fsocial.postservice.dto.comment.CommentDTO;
import com.fsocial.postservice.dto.comment.CommentDTORequest;
import com.fsocial.postservice.dto.comment.CommentResponse;
import com.fsocial.postservice.dto.comment.CommentUpdateDTORequest;
import com.fsocial.postservice.entity.Comment;

import java.util.List;

public interface CommentService {
    CommentResponse addComment(CommentDTORequest comment);

    boolean toggleLikeComment(String commentId, String userId);

    Integer countLike(String commentId, String userId);

    CommentResponse updateComment(CommentUpdateDTORequest comment);

    String deleteComment(String commentID);

    // Methods from timelineService
    List<CommentResponse> getComments(String postId);

    CommentResponse convertToCommentResponse(Comment comment);

    List<CommentDTO> deleteCommentByPostId(String postId);
}
