package com.fsocial.postservice.services;

import com.fsocial.postservice.entity.Post;

import java.util.List;
import java.util.Map;

public interface ScoringService {

    /**
     * raw_engagement = likes×2 + comments×3 + shares×5 (không penalty)
     */
    double calculateRawEngagement(Post post, int commentCount);

    /**
     * global_score = max(0, rawEngagement − ln(age_hours+1)×10)
     */
    double calculateGlobalScore(Post post, int commentCount);

    /**
     * personal_affinity: sum tag weights for post tags, normalized to [0.0, 1.0].
     * Returns 0.5 if user has no interest data (cold start neutral).
     */
    double calculatePersonalAffinity(Map<String, Double> normalizedWeights, List<String> postTags);

    /**
     * final_score = global_score × personal_affinity × social_boost
     * social_boost = 1.5 if authorId is in followingIds, else 1.0
     */
    double calculateFinalScore(Post post, int commentCount,
                               Map<String, Double> normalizedWeights,
                               boolean isFollowingAuthor);
}
