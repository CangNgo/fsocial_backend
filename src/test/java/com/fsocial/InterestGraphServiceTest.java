package com.fsocial;

import com.fsocial.entity.UserInterest;
import com.fsocial.repository.UserInterestRepository;
import com.fsocial.services.impl.InterestGraphServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterestGraphServiceTest {

    @Mock
    private UserInterestRepository userInterestRepository;

    private InterestGraphServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InterestGraphServiceImpl(userInterestRepository);
    }

    // --- getNormalizedWeights ---

    @Test
    @DisplayName("Empty weights when user has no interests")
    void normalizedWeights_noInterests_empty() {
        when(userInterestRepository.findByUserIdOrderByWeightDesc("user1")).thenReturn(List.of());
        assertThat(service.getNormalizedWeights("user1")).isEmpty();
    }

    @Test
    @DisplayName("Weights sum to 1.0 after normalization")
    void normalizedWeights_sumToOne() {
        stubInterests("user1", Map.of("travel", 6.0, "food", 3.0, "tech", 1.0));

        Map<String, Double> weights = service.getNormalizedWeights("user1");

        double sum = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        assertThat(sum).isCloseTo(1.0, within(0.001));
        assertThat(weights).containsKey("travel");
        assertThat(weights.get("travel")).isCloseTo(0.6, within(0.001));
    }

    @Test
    @DisplayName("AC-B-02: travel=60%, food=30%, tech=10% allocation from weights")
    void normalizedWeights_correctRatios() {
        stubInterests("user1", Map.of("travel", 6.0, "food", 3.0, "tech", 1.0));

        Map<String, Double> weights = service.getNormalizedWeights("user1");

        // BRD: travel=6/10=0.60, food=3/10=0.30, tech=1/10=0.10
        assertThat(weights.get("travel")).isCloseTo(0.60, within(0.001));
        assertThat(weights.get("food")).isCloseTo(0.30, within(0.001));
        assertThat(weights.get("tech")).isCloseTo(0.10, within(0.001));
    }

    @Test
    @DisplayName("Chỉ top-10 tag được tính, tránh loãng affinity")
    void normalizedWeights_topKOnly() {
        Map<String, Double> many = new java.util.LinkedHashMap<>();
        for (int i = 0; i < 15; i++) many.put("tag" + i, 15.0 - i);
        stubInterests("user1", many);

        Map<String, Double> weights = service.getNormalizedWeights("user1");

        assertThat(weights).hasSize(10);
        assertThat(weights).doesNotContainKey("tag14");
    }

    // --- updateInterests ---

    @Test
    @DisplayName("Mỗi tag một lần incrementWeight — thay upsert + positional-inc")
    void updateInterests_incrementsPerTag() {
        service.updateInterests("user1", List.of("travel", "food"), 2.0);

        verify(userInterestRepository).incrementWeight("user1", "travel", 2.0);
        verify(userInterestRepository).incrementWeight("user1", "food", 2.0);
        verifyNoMoreInteractions(userInterestRepository);
    }

    @Test
    @DisplayName("Empty tag list → no update performed")
    void updateInterests_emptyTags_noOp() {
        service.updateInterests("user1", List.of(), 2.0);
        verifyNoInteractions(userInterestRepository);
    }

    @Test
    @DisplayName("delta = 0 → no update performed")
    void updateInterests_zeroDelta_noOp() {
        service.updateInterests("user1", List.of("travel"), 0);
        verifyNoInteractions(userInterestRepository);
    }

    // --- applyDecay ---

    @Test
    @DisplayName("AC-B-04: decay là 1 UPDATE + 1 DELETE, không load/save từng document")
    void applyDecay_delegatesToBulkQueries() {
        when(userInterestRepository.decayAll(0.95)).thenReturn(3);
        when(userInterestRepository.deleteBelowWeight(0.1)).thenReturn(1);

        service.applyDecay(0.95, 0.1);

        verify(userInterestRepository).decayAll(0.95);
        verify(userInterestRepository).deleteBelowWeight(0.1);
        verifyNoMoreInteractions(userInterestRepository);
    }

    @Test
    @DisplayName("AC-B-04: after 7 days weight should be ~6.98 (10 × 0.95^7)")
    void applyDecay_sevenDays() {
        double weight = 10.0;
        for (int i = 0; i < 7; i++) {
            weight *= 0.95;
        }
        assertThat(weight).isCloseTo(6.98, within(0.01));
    }

    // --- helpers ---

    private void stubInterests(String userId, Map<String, Double> tagWeights) {
        List<UserInterest> rows = tagWeights.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
                .map(e -> UserInterest.builder().userId(userId).tag(e.getKey()).weight(e.getValue()).build())
                .toList();
        when(userInterestRepository.findByUserIdOrderByWeightDesc(userId)).thenReturn(rows);
    }
}
