package com.fsocial.services;

import com.fsocial.entity.Post;

import java.util.List;
import java.util.Map;

/**
 * likeCount và tags giờ phải truyền vào: sau khi chuẩn hóa sang Postgres chúng nằm ở
 * bảng post_like / post_tag, không còn là mảng embed trong Post.
 */
public interface ScoringService {

    /**
     * raw_engagement = likes×2 + comments×3 + shares×5 (không penalty)
     */
    double calculateRawEngagement(Post post, int likeCount, int commentCount);

    /**
     * global_score = max(0, rawEngagement − ln(age_hours+1)×10)
     */
    double calculateGlobalScore(Post post, int likeCount, int commentCount);

    /**
     * personal_affinity: sum tag weights for post tags, normalized to [0.0, 1.0].
     * Returns 0.5 if user has no interest data (cold start neutral).
     */
    double calculatePersonalAffinity(Map<String, Double> normalizedWeights, List<String> postTags);

    /**
     * final_score = global_score × personal_affinity × social_boost
     * social_boost = 1.5 if authorId is in followingIds, else 1.0
     */
    double calculateFinalScore(Post post, int likeCount, int commentCount,
                               Map<String, Double> normalizedWeights,
                               List<String> postTags,
                               boolean isFollowingAuthor);
}
