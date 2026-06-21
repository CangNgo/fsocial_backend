package com.fsocial.postservice.services.impl;

import com.fsocial.postservice.entity.Post;
import com.fsocial.postservice.services.ScoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ScoringServiceImpl implements ScoringService {

    private static final double SOCIAL_BOOST_MULTIPLIER = 1.5;
    private static final double COLD_START_AFFINITY = 0.5;

    @Override
    public double calculateGlobalScore(Post post, int commentCount) {
        int likeCount = post.getLikes() == null ? 0 : post.getLikes().size();
        int shareCount = post.getShareCount();

        double rawScore = likeCount * 2.0
                + commentCount * 3.0
                + shareCount * 5.0
                - timePenalty(post.getCreateDatetime());

        return Math.max(0.0, rawScore);
    }

    /**
     * time_penalty = ln(age_hours + 1) × 10
     */
    private double timePenalty(LocalDateTime createDatetime) {
        if (createDatetime == null) return 0.0;
        long ageHours = ChronoUnit.HOURS.between(createDatetime, LocalDateTime.now());
        ageHours = Math.max(0, ageHours);
        return Math.log(ageHours + 1) * 10.0;
    }

    @Override
    public double calculatePersonalAffinity(Map<String, Double> normalizedWeights, List<String> postTags) {
        if (normalizedWeights == null || normalizedWeights.isEmpty()) {
            return COLD_START_AFFINITY;
        }
        if (postTags == null || postTags.isEmpty()) {
            return COLD_START_AFFINITY;
        }

        double sum = postTags.stream()
                .mapToDouble(tag -> normalizedWeights.getOrDefault(tag, 0.0))
                .sum();

        // Already normalized weights sum to at most 1.0 per tag, but multiple tags can exceed 1.0
        // Cap at 1.0
        return Math.min(1.0, sum);
    }

    @Override
    public double calculateFinalScore(Post post, int commentCount,
                                      Map<String, Double> normalizedWeights,
                                      boolean isFollowingAuthor) {
        double globalScore = calculateGlobalScore(post, commentCount);
        double affinity = calculatePersonalAffinity(normalizedWeights, post.getTags());
        double socialBoost = isFollowingAuthor ? SOCIAL_BOOST_MULTIPLIER : 1.0;

        double finalScore = globalScore * affinity * socialBoost;
        log.debug("Post {} score: global={:.2f}, affinity={:.2f}, boost={}, final={:.2f}",
                post.getId(), globalScore, affinity, socialBoost, finalScore);
        return finalScore;
    }
}
