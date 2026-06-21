package com.fsocial.postservice;

import com.fsocial.postservice.entity.Owner;
import com.fsocial.postservice.entity.Post;
import com.fsocial.postservice.services.impl.ScoringServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

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
        Post newer = postWithAge(1, 5, 3, 2);   // 1 hour old
        Post older = postWithAge(24, 5, 3, 2);  // 24 hours old

        double scoreNewer = scoringService.calculateGlobalScore(newer, 3);
        double scoreOlder = scoringService.calculateGlobalScore(older, 3);

        assertThat(scoreNewer).isGreaterThan(scoreOlder);
    }

    @Test
    @DisplayName("AC-A-02: time penalty difference ~ 25.1 points for 1h vs 24h")
    void globalScore_timePenaltyDiff() {
        // Use 50 likes so raw score = 100 (well above penalty), preventing 0-clamp
        // BRD: diff = ln(25)×10 − ln(2)×10 ≈ 32.19 − 6.93 ≈ 25.1
        Post post1h  = postWithAge(1,  50, 0, 0);
        Post post24h = postWithAge(24, 50, 0, 0);

        double s1  = scoringService.calculateGlobalScore(post1h,  0);
        double s24 = scoringService.calculateGlobalScore(post24h, 0);

        double diff = s1 - s24;
        assertThat(diff).isCloseTo(25.1, within(1.0));
    }

    @Test
    @DisplayName("Like weight=2, comment weight=3, share weight=5")
    void globalScore_engagementWeights() {
        Post likeOnly    = postWithAge(0, 1, 0, 0);
        Post commentOnly = postWithAge(0, 0, 1, 0);
        Post shareOnly   = postWithAge(0, 0, 0, 1);

        double sLike    = scoringService.calculateGlobalScore(likeOnly,    0);
        double sComment = scoringService.calculateGlobalScore(commentOnly, 1);
        double sShare   = scoringService.calculateGlobalScore(shareOnly,   0);

        assertThat(sShare).isGreaterThan(sComment);
        assertThat(sComment).isGreaterThan(sLike);
        assertThat(sLike).isCloseTo(2.0, within(0.01));
    }

    @Test
    @DisplayName("global_score is never negative")
    void globalScore_neverNegative() {
        Post veryOldPost = postWithAge(10000, 0, 0, 0);
        double score = scoringService.calculateGlobalScore(veryOldPost, 0);
        assertThat(score).isGreaterThanOrEqualTo(0.0);
    }

    // --- personal_affinity tests ---

    @Test
    @DisplayName("Cold start: no weights → affinity = 0.5")
    void affinity_coldStart_returnsHalf() {
        double affinity = scoringService.calculatePersonalAffinity(Map.of(), List.of("travel"));
        assertThat(affinity).isEqualTo(0.5);
    }

    @Test
    @DisplayName("Affinity = 0.5 when post has no tags")
    void affinity_noTags_returnsHalf() {
        Map<String, Double> weights = Map.of("travel", 0.6, "food", 0.4);
        double affinity = scoringService.calculatePersonalAffinity(weights, List.of());
        assertThat(affinity).isEqualTo(0.5);
    }

    @Test
    @DisplayName("AC-A-01: different users → different affinity for same post tags")
    void affinity_differentUsersGetDifferentScores() {
        // Alice loves travel; Bob loves tech
        Map<String, Double> alice = Map.of("travel", 1.0);
        Map<String, Double> bob   = Map.of("tech", 1.0);
        List<String> travelPost   = List.of("travel");

        double aliceAffinity = scoringService.calculatePersonalAffinity(alice, travelPost);
        double bobAffinity   = scoringService.calculatePersonalAffinity(bob,   travelPost);

        assertThat(aliceAffinity).isGreaterThan(bobAffinity);
    }

    @Test
    @DisplayName("Affinity capped at 1.0 even with multiple matching tags")
    void affinity_cappedAt1() {
        Map<String, Double> weights = Map.of("travel", 0.7, "food", 0.6);
        List<String> tags = List.of("travel", "food");
        double affinity = scoringService.calculatePersonalAffinity(weights, tags);
        assertThat(affinity).isLessThanOrEqualTo(1.0);
    }

    // --- final_score + social_boost tests ---

    @Test
    @DisplayName("social_boost × 1.5 when user follows author")
    void finalScore_socialBoostApplied() {
        Post post = postWithAge(1, 10, 5, 2);
        Map<String, Double> weights = Map.of("travel", 1.0);
        post.setTags(List.of("travel"));

        double withBoost    = scoringService.calculateFinalScore(post, 5, weights, true);
        double withoutBoost = scoringService.calculateFinalScore(post, 5, weights, false);

        assertThat(withBoost).isEqualTo(withoutBoost * 1.5, within(0.001));
    }

    @Test
    @DisplayName("final_score = global × affinity × boost")
    void finalScore_formula() {
        Post post = postWithAge(0, 5, 0, 0); // 5 likes = 10 pts, age 0 → penalty ln(1)×10=0
        post.setTags(List.of("travel"));
        Map<String, Double> weights = Map.of("travel", 0.8); // affinity = 0.8

        double expected = 10.0 * 0.8 * 1.0; // no social boost
        double actual = scoringService.calculateFinalScore(post, 0, weights, false);

        assertThat(actual).isCloseTo(expected, within(0.5)); // small tolerance for age drift
    }

    // --- helpers ---

    private Post postWithAge(int ageHours, int likes, int comments, int shares) {
        List<String> likeList = java.util.Collections.nCopies(likes, "user");
        Post post = new Post();
        post.setLikes(new java.util.ArrayList<>(likeList));
        post.setShareCount(shares);
        post.setTags(List.of());
        post.setCreateDatetime(LocalDateTime.now().minusHours(ageHours));
        post.setOwner(Owner.builder().userId("owner1").build());
        return post;
    }
}
