package com.fsocial.postservice.services;

import com.fsocial.postservice.dto.post.PostResponse;

import java.util.List;

public interface FeedService {

    /**
     * Builds a personalized feed for the user.
     * Applies Feature A (scoring) + Feature B (Exploit/Explore/Wildcard).
     * Falls back to chronological feed for new users with no interests.
     *
     * @param userId    requesting user
     * @param feedSize  number of posts to return (typically 10–20)
     */
    List<PostResponse> buildPersonalizedFeed(String userId, int feedSize);

    /**
     * Records that the user has seen a post (updates seen_posts collection).
     */
    void markSeen(String userId, String postId);

    /**
     * Returns list of post IDs the user has already seen (within 30 days).
     */
    List<String> getSeenPostIds(String userId);
}
