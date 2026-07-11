package com.fsocial.postservice.controller;

import com.fsocial.postservice.dto.ApiResponse;
import com.fsocial.postservice.dto.comment.CommentDTORequest;
import com.fsocial.postservice.dto.comment.CommentResponse;
import com.fsocial.postservice.dto.comment.CommentUpdateDTORequest;
import com.fsocial.postservice.dto.comment.LikeCommentDTO;
import com.fsocial.postservice.entity.Comment;
import com.fsocial.postservice.exception.AppCheckedException;
import com.fsocial.postservice.exception.StatusCode;
import com.fsocial.postservice.services.CommentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/comment")
@Slf4j
public class CommentController {

    CommentService commentService;

    @PostMapping
    public ApiResponse<Comment> createComment(CommentDTORequest request) throws AppCheckedException {
        Comment comment = commentService.addComment(request);
        return ApiResponse.<Comment>builder()
                .data(comment)
                .message("Comment created successfully")
                .build();
    }

    @PostMapping("/like")
    public ApiResponse<Map<String, Object>> likeComment(@RequestBody @Valid LikeCommentDTO dto) throws AppCheckedException {
        boolean like = commentService.toggleLikeComment(dto.getCommentId(), dto.getUserId());
        Map<String, Object> result = new HashMap<>();
        result.put("like", like);
        result.put("userid", dto.getUserId());
        return ApiResponse.<Map<String, Object>>builder()
                .data(result)
                .message(like ? "Thích bình luận thành công" : "Hủy thích bình luận thành công")
                .build();
    }

    @PutMapping
    public ApiResponse<Comment> updateComment(@RequestBody @Valid CommentUpdateDTORequest dto) throws AppCheckedException {
        Comment update = commentService.updateComment(dto);
        return ApiResponse.<Comment>builder()
                .message("Comment updated successfully")
                .data(update)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteComment(@PathVariable("id") String id) {
        return ApiResponse.<String>builder()
                .message("Comment deleted successfully")
                .data(commentService.deleteComment(id))
                .build();
    }

    @GetMapping()
    public ApiResponse<List<CommentResponse>> getComment(@RequestParam("postId") String postId) {
        List<CommentResponse> commentByPostId = commentService.getComments(postId);
        return ApiResponse.<List<CommentResponse>>builder()
                .data(commentByPostId)
                .message("Comment get by postId successfully")
                .build();
    }
}
