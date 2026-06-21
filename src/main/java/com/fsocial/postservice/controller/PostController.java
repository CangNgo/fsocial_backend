package com.fsocial.postservice.controller;

import com.fsocial.postservice.dto.ApiResponse;
import com.fsocial.postservice.dto.Response;
import com.fsocial.postservice.dto.post.*;
import com.fsocial.postservice.entity.Post;
import com.fsocial.postservice.exception.AppCheckedException;
import com.fsocial.postservice.exception.StatusCode;
import com.fsocial.postservice.services.PostService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/actions")
@Tag(name = "Post controller")
public class PostController {
    PostService postService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PostDTO> createPost(@Valid @ModelAttribute PostDTORequest request, @AuthenticationPrincipal Jwt jwt) throws AppCheckedException {
        //Bai viet phai co noi dung hoac hinh anh

        String userId = jwt.getSubject();
        request.setUserId(userId);

        if ((request.getText() == null || request.getText().isEmpty())
                && (request.getMedia() == null || request.getMedia().length == 0)) {
            throw new AppCheckedException("Bài viết phải có nội dung, hình ảnh hoặc video", StatusCode.NOT_CONTENT);
        }

        PostDTO post = postService.createPost(request);
        log.info("Đăng bài viết thành công");
        return ApiResponse.<PostDTO>builder()
                .data(post)
                .statusCode(StatusCode.CREATE_POST_SUCCESS.getCode())
                .message("Tạo bài viết thành công")
                .build();
    }

    @PutMapping
    public ResponseEntity<Response> updatePost(
            @RequestParam("text") String text,
            @RequestParam("html") String html,
            @RequestParam("postId") String postId) throws AppCheckedException {

        //check postId
        if (postId == null || postId.isEmpty()) {
            throw new AppCheckedException("Mã bài viết không được để trống", StatusCode.POST_NOT_FOUND);
        }

        //Mapping DTO
        PostDTORequest postDTO = PostDTORequest.builder()
                .text(text)
                .html(html)
                .build();

        //Update Post
        PostDTO post = postService.updatePost(postDTO, postId);

        //return result
        return ResponseEntity.ok(Response.builder()
                .data(post)
                .message("Cập nhật bài viết thành công")
                .build());
    }

    @DeleteMapping
    public ResponseEntity<Response> deletePost(
            @RequestParam("postId") String postId) {
        postService.deletePost(postId);
        return ResponseEntity.ok(Response.builder()
                .message("Xóa bài viết thành công")
                .build());
    }

    //Like Post
    @PostMapping("/like")
    public ResponseEntity<Response> likePost(@RequestBody LikePostDTO likeDTO) throws Exception {
        boolean like = postService.toggleLike(likeDTO.getPostId(), likeDTO.getUserId());

        Map<String, Object> map = new HashMap<>();
        map.put("like", like);
        map.put("userId", likeDTO.getUserId());
        return ResponseEntity.ok(Response.builder()
                .data(map)
                .message(like ? "Thích bài viết thành công" : "bỏ thích bài viết thành công")
                .build());
    }

    @PostMapping("/share")
    public ResponseEntity<Response> sharePost(@Valid PostShareDTORequest share) {
        PostDTO post = postService.sharePost(share);
        return ResponseEntity.ok(Response.builder()
                .data(post)
                .statusCode(StatusCode.OK.getCode())
                .message("Chia sẽ bài viết thành công")
                .build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Post>> getPostsByUser(@PathVariable String userId, @RequestParam String requesterId) {
        List<Post> posts = postService.getPostsByUser(userId, requesterId);
        if (posts.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(posts);
    }

    @GetMapping
    public ResponseEntity<Response> getPosts(@RequestParam(value = "userId") String userId) throws AppCheckedException {
        List<PostResponse> posts = postService.getPostsByUserId(userId);
        log.info("Lấy thông tin bài viết thành công");
        return ResponseEntity.ok(Response.builder()
                .message("Lấy bài đăng thành công")
                .dateTime(LocalDateTime.now())
                .data(posts)
                .build());
    }

    @GetMapping("/following")
    public ResponseEntity<Response> getPostsByFollowing(@RequestParam(value = "userId") String userId) throws AppCheckedException {
        List<PostResponse> posts = postService.getPostByFollowing(userId);
        log.info("Lấy thông tin bài viết theo following thành công");
        return ResponseEntity.ok(Response.builder()
                .message("Lấy bài đăng theo following thành công")
                .dateTime(LocalDateTime.now())
                .data(posts)
                .build());
    }

    @GetMapping("/find")
    public ResponseEntity<Response> findPost(@RequestParam("find_post") String findString,
                                             @RequestParam("user_id") String userId) throws AppCheckedException {
        List<PostResponse> findByText = postService.findByText(findString, userId);
        log.info("Tìm kiếm bài đăng theo text thành công");
        return ResponseEntity.ok(Response.builder()
                .message("Lấy bài đăng thành công")
                .dateTime(LocalDateTime.now())
                .data(findByText)
                .build());
    }

    @GetMapping("/getpost_id")
    public ResponseEntity<Response> getPostId(@RequestParam("post_id") String postId, @RequestParam("user_id") String userId) throws AppCheckedException {
        PostResponse result = postService.getPostById(postId, userId);
        log.info("Tìm kiếm bài đăng theo id thành công");
        return ResponseEntity.ok(Response.builder()
                .message("Lấy bài đăng thành công")
                .dateTime(LocalDateTime.now())
                .data(result)
                .build());
    }

    //thống kê số lượng bài viết
    @GetMapping("/statistics_post_today")
    public ResponseEntity<Response> getPosttStatistics(@RequestParam("date_time") String dateTime) {
        LocalDate date = LocalDate.parse(dateTime);
        LocalDateTime startDate = date.atStartOfDay();
        LocalDateTime endDate = date.atTime(23, 59, 59);
        List<PostStatisticsDTO> result = postService.countStatisticsPostToday(startDate, endDate);
        log.info("Lấy thông tin thống kê theo {} thành công", date);
        return ResponseEntity.ok().body(Response.builder()
                .data(result)
                .message("Lấy toàn bộ danh sách thống kê số lượng bài viết trong ngày " + date + "  thành công")
                .build());
    }

    @GetMapping("/statistics_post_start_end")
    public ResponseEntity<Response> getPostStatistics(@RequestParam("startDate") String startDateRe, @RequestParam("endDate") String endDateRe) {
        LocalDate start = LocalDate.parse(startDateRe);
        LocalDate end = LocalDate.parse(endDateRe);
        LocalDateTime startDate = start.atStartOfDay();
        LocalDateTime endDate = end.atTime(23, 59, 59);

        return ResponseEntity.ok().body(Response.builder()
                .data(postService.countStatisticsPostLongDay(startDate, endDate))
                .message("Lấy toàn bộ danh sách thống kê số lượng bài viết từ ngày " + startDate + " đến " + endDate + "  thành công")
                .build());
    }

}
