package com.fsocial.postservice.services;

import com.fsocial.postservice.dto.post.*;
import com.fsocial.postservice.dto.response.SearchPageResponse;
import com.fsocial.postservice.entity.Post;

import java.util.List;

public interface PostService {
    PostDTO createPost(PostDTORequest request);
    PostDTO updatePost(PostDTORequest post, String postId);
    void deletePost(String postId) ;
    boolean toggleLike(String postId, String userId) throws Exception;
    Integer CountLike(String postId, String userId);
    PostDTO sharePost (PostShareDTORequest dto );
    List<Post> getPostsByUser(String userId, String requesterId);

    // Methods from timelineService
    List<PostResponse> getPostsByUserId(String userId, int feedSize);

//    com.fsocial.postservice.dto.profile.ProfileResponse getProfile(String id);

    SearchPageResponse<PostResponse> findByText(String text, String userId, int page, int size);

    PostResponse getPostById(String postId, String userId);

    List<PostStatisticsDTO> countStatisticsPostToday(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    List<PostStatisticsLongDateDTO> countStatisticsPostLongDay(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    List<PostResponse> getPostByFollowing(String userId);
}
