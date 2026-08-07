package com.fsocial;

import com.fsocial.entity.Post;
import com.fsocial.services.impl.ScoringServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Like/tag không còn nằm trong Post — đếm được truyền vào từ post_like / comment,
 * tag truyền vào từ post_tag. Công thức không đổi.
 */
class ScoringServiceTest {

    private ScoringServiceImpl scoringService;

    @BeforeEach
    void setUp() {
        scoringService = new ScoringServiceImpl();
    }

    // --- global_score tests ---

    @Test
    @DisplayName("AC-A-02: newer post scores higher than older post with same engagement")
    void globalScore_newerPostScoresHigher() {
        Post newer = postWithAge(1, 2);
        Post older = postWithAge(24, 2);

        double scoreNewer = scoringService.calculateGlobalScore(newer, 5, 3);
        double scoreOlder = scoringService.calculateGlobalScore(older, 5, 3);

        assertThat(scoreNewer).isGreaterThan(scoreOlder);
    }

    @Test
    @DisplayName("AC-A-02: time penalty difference ~ 25.1 points for 1h vs 24h")
    void globalScore_timePenaltyDiff() {
        // 50 likes → raw score = 100 (well above penalty), preventing 0-clamp
        // BRD: diff = ln(25)×10 − ln(2)×10 ≈ 32.19 − 6.93 ≈ 25.1
        Post post1h = postWithAge(1, 0);
        Post post24h = postWithAge(24, 0);

        double s1 = scoringService.calculateGlobalScore(post1h, 50, 0);
        double s24 = scoringService.calculateGlobalScore(post24h, 50, 0);

        assertThat(s1 - s24).isCloseTo(25.1, within(1.0));
    }

    @Test
    @DisplayName("Like weight=2, comment weight=3, share weight=5")
    void globalScore_engagementWeights() {
        double sLike = scoringService.calculateGlobalScore(postWithAge(0, 0), 1, 0);
        double sComment = scoringService.calculateGlobalScore(postWithAge(0, 0), 0, 1);
        double sShare = scoringService.calculateGlobalScore(postWithAge(0, 1), 0, 0);

        assertThat(sShare).isGreaterThan(sComment);
        assertThat(sComment).isGreaterThan(sLike);
        assertThat(sLike).isCloseTo(2.0, within(0.01));
    }

    @Test
    @DisplayName("global_score is never negative")
    void globalScore_neverNegative() {
        double score = scoringService.calculateGlobalScore(postWithAge(10000, 0), 0, 0);
        assertThat(score).isGreaterThanOrEqualTo(0.0);
    }

    // --- personal_affinity tests ---

    @Test
    @DisplayName("Cold start: no weights → affinity = 0.5")
    void affinity_coldStart_returnsHalf() {
        assertThat(scoringService.calculatePersonalAffinity(Map.of(), List.of("travel"))).isEqualTo(0.5);
    }

    @Test
    @DisplayName("Affinity = 0.5 when post has no tags")
    void affinity_noTags_returnsHalf() {
        Map<String, Double> weights = Map.of("travel", 0.6, "food", 0.4);
        assertThat(scoringService.calculatePersonalAffinity(weights, List.of())).isEqualTo(0.5);
    }

    @Test
    @DisplayName("AC-A-01: different users → different affinity for same post tags")
    void affinity_differentUsersGetDifferentScores() {
        // Alice loves travel; Bob loves tech
        Map<String, Double> alice = Map.of("travel", 1.0);
        Map<String, Double> bob = Map.of("tech", 1.0);
        List<String> travelPost = List.of("travel");

        assertThat(scoringService.calculatePersonalAffinity(alice, travelPost))
                .isGreaterThan(scoringService.calculatePersonalAffinity(bob, travelPost));
    }

    @Test
    @DisplayName("Affinity capped at 1.0 even with multiple matching tags")
    void affinity_cappedAt1() {
        Map<String, Double> weights = Map.of("travel", 0.7, "food", 0.6);
        assertThat(scoringService.calculatePersonalAffinity(weights, List.of("travel", "food")))
                .isLessThanOrEqualTo(1.0);
    }

    // --- final_score + social_boost tests ---

    @Test
    @DisplayName("social_boost × 1.5 when user follows author")
    void finalScore_socialBoostApplied() {
        Post post = postWithAge(1, 2);
        Map<String, Double> weights = Map.of("travel", 1.0);
        List<String> tags = List.of("travel");

        double withBoost = scoringService.calculateFinalScore(post, 10, 5, weights, tags, true);
        double withoutBoost = scoringService.calculateFinalScore(post, 10, 5, weights, tags, false);

        assertThat(withBoost).isEqualTo(withoutBoost * 1.5, within(0.001));
    }

    @Test
    @DisplayName("final_score = global × affinity × boost")
    void finalScore_formula() {
        Post post = postWithAge(0, 0); // age 0 → penalty ln(1)×10 = 0
        Map<String, Double> weights = Map.of("travel", 0.8); // affinity = 0.8

        double expected = 10.0 * 0.8 * 1.0; // 5 likes = 10 pts, no social boost
        double actual = scoringService.calculateFinalScore(post, 5, 0, weights, List.of("travel"), false);

        assertThat(actual).isCloseTo(expected, within(0.5)); // small tolerance for age drift
    }

    // --- helpers ---

    private Post postWithAge(int ageHours, int shares) {
        Post post = new Post();
        post.setShareCount(shares);
        post.setCreateDatetime(LocalDateTime.now().minusHours(ageHours));
        return post;
    }
}
