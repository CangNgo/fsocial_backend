package com.fsocial.services;

import com.fsocial.dto.comment.CommentDTO;
import com.fsocial.dto.comment.CommentDTORequest;
import com.fsocial.dto.comment.CommentResponse;
import com.fsocial.dto.comment.CommentUpdateDTORequest;
import com.fsocial.entity.Comment;

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
