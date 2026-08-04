package com.fsocial.postservice.controller;

import com.fsocial.postservice.dto.ApiResponse;
import com.fsocial.postservice.dto.comment.CommentDTORequest;
import com.fsocial.postservice.dto.comment.CommentResponse;
import com.fsocial.postservice.dto.comment.CommentUpdateDTORequest;
import com.fsocial.postservice.dto.comment.LikeCommentDTO;
import com.fsocial.postservice.services.CommentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

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
    public ApiResponse<CommentResponse> createComment(
            @AuthenticationPrincipal Jwt jwt,
            CommentDTORequest request
    ) {
        request.setUserId(jwt.getSubject());
        return ApiResponse.<CommentResponse>builder()
                .data(commentService.addComment(request))
                .message("Comment created successfully")
                .build();
    }

    @PostMapping("/like")
    public ApiResponse<Map<String, Object>> likeComment(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid LikeCommentDTO dto
    ) {
        String userId = jwt.getSubject();
        boolean like = commentService.toggleLikeComment(dto.getCommentId(), userId);
        Map<String, Object> result = new HashMap<>();
        result.put("like", like);
        result.put("userid", userId);
        return ApiResponse.<Map<String, Object>>builder()
                .data(result)
                .message(like ? "Thích bình luận thành công" : "Hủy thích bình luận thành công")
                .build();
    }

    @PutMapping
    public ApiResponse<CommentResponse> updateComment(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid CommentUpdateDTORequest dto
    ) {
        dto.setUserId(jwt.getSubject());
        return ApiResponse.<CommentResponse>builder()
                .message("Comment updated successfully")
                .data(commentService.updateComment(dto))
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
