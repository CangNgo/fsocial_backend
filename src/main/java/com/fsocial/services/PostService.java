package com.fsocial.services;

import com.fsocial.dto.post.*;
import com.fsocial.dto.response.SearchPageResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface PostService {
    PostDTO createPost(PostDTORequest request);
    PostDTO updatePost(PostDTORequest post, String postId);
    void deletePost(String postId) ;
    boolean toggleLike(String postId, String userId) throws Exception;
    Integer CountLike(String postId, String userId);
    PostDTO sharePost (PostShareDTORequest dto );
    List<PostResponse> getPostsByUser(String userId, String requesterId);

    // Methods from timelineService
    List<PostResponse> getPostsByUserId(String userId, int feedSize);

//    com.fsocial.postservice.dto.profile.ProfileResponse getProfile(String id);

    SearchPageResponse<PostResponse> findByText(String text, String userId, int page, int size);

    PostResponse getPostById(String postId, String userId);

    List<PostStatisticsDTO> countStatisticsPostToday(LocalDateTime startDate, LocalDateTime endDate);

    List<PostStatisticsLongDateDTO> countStatisticsPostLongDay(LocalDateTime startDate, LocalDateTime endDate);

    List<PostResponse> getPostByFollowing(String userId);

    List<PostResponse> getMyPost(String userId);
}
