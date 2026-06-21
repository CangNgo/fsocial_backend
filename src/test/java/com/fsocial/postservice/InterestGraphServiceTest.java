package com.fsocial.postservice;

import com.fsocial.postservice.entity.UserInterests;
import com.fsocial.postservice.repository.UserInterestsRepository;
import com.fsocial.postservice.services.impl.InterestGraphServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterestGraphServiceTest {

    @Mock
    private UserInterestsRepository userInterestsRepository;
    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private com.mongodb.client.result.UpdateResult updateResult;

    private InterestGraphServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InterestGraphServiceImpl(userInterestsRepository, mongoTemplate);
    }

    // --- getNormalizedWeights tests ---

    @Test
    @DisplayName("Empty weights when user has no interests")
    void normalizedWeights_noInterests_empty() {
        when(userInterestsRepository.findByUserId("user1")).thenReturn(Optional.empty());
        Map<String, Double> result = service.getNormalizedWeights("user1");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Weights sum to 1.0 after normalization")
    void normalizedWeights_sumToOne() {
        UserInterests ui = buildInterests("user1",
                Map.of("travel", 6.0, "food", 3.0, "tech", 1.0));
        when(userInterestsRepository.findByUserId("user1")).thenReturn(Optional.of(ui));

        Map<String, Double> weights = service.getNormalizedWeights("user1");

        double sum = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        assertThat(sum).isCloseTo(1.0, within(0.001));
        assertThat(weights).containsKey("travel");
        assertThat(weights.get("travel")).isCloseTo(0.6, within(0.001));
    }

    @Test
    @DisplayName("AC-B-02: travel=60%, food=30%, tech=10% allocation from weights")
    void normalizedWeights_correctRatios() {
        UserInterests ui = buildInterests("user1",
                Map.of("travel", 6.0, "food", 3.0, "tech", 1.0));
        when(userInterestsRepository.findByUserId("user1")).thenReturn(Optional.of(ui));

        Map<String, Double> weights = service.getNormalizedWeights("user1");

        // BRD: travel=6/10=0.60, food=3/10=0.30, tech=1/10=0.10
        assertThat(weights.get("travel")).isCloseTo(0.60, within(0.001));
        assertThat(weights.get("food")).isCloseTo(0.30, within(0.001));
        assertThat(weights.get("tech")).isCloseTo(0.10, within(0.001));
    }

    // --- applyDecay tests ---

    @Test
    @DisplayName("AC-B-04: weight decays by factor 0.95 per day")
    void applyDecay_reducesWeights() {
        UserInterests ui = buildInterests("user1", Map.of("travel", 10.0));
        when(userInterestsRepository.findAll()).thenReturn(List.of(ui));

        service.applyDecay(0.95, 0.1);

        ArgumentCaptor<UserInterests> captor = ArgumentCaptor.forClass(UserInterests.class);
        verify(userInterestsRepository).save(captor.capture());

        double newWeight = captor.getValue().getInterests().get(0).getWeight();
        assertThat(newWeight).isCloseTo(9.5, within(0.001)); // 10 × 0.95
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

    @Test
    @DisplayName("Entries below threshold are removed after decay")
    void applyDecay_removesLowWeightEntries() {
        UserInterests ui = buildInterests("user1", Map.of("travel", 10.0, "old_tag", 0.09));
        when(userInterestsRepository.findAll()).thenReturn(List.of(ui));

        service.applyDecay(0.95, 0.1);

        ArgumentCaptor<UserInterests> captor = ArgumentCaptor.forClass(UserInterests.class);
        verify(userInterestsRepository).save(captor.capture());

        List<UserInterests.InterestItem> remaining = captor.getValue().getInterests();
        // old_tag (0.09) < threshold (0.1) → removed; travel (9.5) kept
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getTag()).isEqualTo("travel");
    }

    @Test
    @DisplayName("Empty tag list → no update performed")
    void updateInterests_emptyTags_noOp() {
        service.updateInterests("user1", List.of(), 2.0);
        verifyNoInteractions(mongoTemplate);
    }

    // --- helpers ---

    private UserInterests buildInterests(String userId, Map<String, Double> tagWeights) {
        List<UserInterests.InterestItem> items = tagWeights.entrySet().stream()
                .map(e -> UserInterests.InterestItem.builder()
                        .tag(e.getKey())
                        .weight(e.getValue())
                        .updatedAt(LocalDateTime.now())
                        .build())
                .toList();
        return UserInterests.builder()
                .userId(userId)
                .interests(new ArrayList<>(items))
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
