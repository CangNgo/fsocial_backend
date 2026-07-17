package com.fsocial.postservice.controller;

import com.fsocial.postservice.dto.ApiResponse;
import com.fsocial.postservice.dto.replyComment.LikeReplyCommentDTO;
import com.fsocial.postservice.dto.replyComment.ReplyCommentRequest;
import com.fsocial.postservice.dto.replyComment.ReplyCommentResponse;
import com.fsocial.postservice.dto.replyComment.ReplyCommentUpdateDTORequest;
import com.fsocial.postservice.entity.ReplyComment;
import com.fsocial.postservice.services.ReplyCommentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/comment/reply")
public class ReplyCommentController {

    ReplyCommentService replyCommentService;

    @PostMapping("/like")
    public ApiResponse<Map<String, Boolean>> likeReplyComment(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid LikeReplyCommentDTO request
    ) {
        request.setUserId(jwt.getSubject());
        boolean like = replyCommentService.likeReplyComment(request);
        Map<String, Boolean> map = new HashMap<>();
        map.put("like", like);
        return ApiResponse.<Map<String, Boolean>>builder()
                .data(map)
                .dateTime(LocalDateTime.now())
                .message(like ? "Like thành công" : "Bỏ like thành công")
                .build();
    }

    @PostMapping
    public ApiResponse<ReplyComment> replyComment(
            @AuthenticationPrincipal Jwt jwt,
            ReplyCommentRequest request
    ) throws IOException {
        request.setUserId(jwt.getSubject());
        ReplyComment response = replyCommentService.addReplyComment(request);

        return ApiResponse.<ReplyComment>builder()
                .data(response)
                .dateTime(LocalDateTime.now())
                .message("Reply comment thành công")
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteReplyComment(@PathVariable("id") String id) {
        return ApiResponse.<String>builder()
                .data(replyCommentService.deleteReplyComment(id))
                .message("Delete reply comment successfully")
                .build();
    }

    @PutMapping
    public ApiResponse<ReplyComment> updateReplyComment(
            @AuthenticationPrincipal Jwt jwt,
            ReplyCommentUpdateDTORequest request
    ) {
        request.setUserId(jwt.getSubject());
        ReplyComment update = replyCommentService.updateReplyComment(request);
        return ApiResponse.<ReplyComment>builder()
                .data(update)
                .message("Update reply comment successfully")
                .build();
    }

    // API from timelineService
    @GetMapping
    public ApiResponse<List<ReplyCommentResponse>> getReplyCommentByCommentId(@RequestParam("comment_id") String commentId) {
        return ApiResponse.<List<ReplyCommentResponse>>builder()
                .data(replyCommentService.getReplyCommentsByCommentId(commentId))
                .dateTime(LocalDateTime.now())
                .statusCode(200)
                .message("Lấy thông tin trả lời bình luận thành công")
                .build();
    }
}
