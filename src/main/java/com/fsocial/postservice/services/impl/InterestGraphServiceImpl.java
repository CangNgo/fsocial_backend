package com.fsocial.postservice.services.impl;

import com.fsocial.postservice.entity.UserInterest;
import com.fsocial.postservice.repository.UserInterestRepository;
import com.fsocial.postservice.services.InterestGraphService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterestGraphServiceImpl implements InterestGraphService {

    private static final int TOP_K_INTERESTS = 10;

    private final UserInterestRepository userInterestRepository;

    @Override
    @Transactional
    public void updateInterests(String userId, List<String> tags, double deltaWeight) {
        if (tags == null || tags.isEmpty() || deltaWeight == 0) return;

        for (String tag : tags) {
            userInterestRepository.incrementWeight(userId, tag, deltaWeight);
        }
        log.debug("Updated interests for user {} tags {} delta={}", userId, tags, deltaWeight);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Double> getRawWeights(String userId) {
        return userInterestRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(UserInterest::getTag, UserInterest::getWeight));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Double> getNormalizedWeights(String userId) {
        // Take only top-K by weight to prevent affinity dilution
        Map<String, Double> topK = userInterestRepository.findByUserIdOrderByWeightDesc(userId).stream()
                .limit(TOP_K_INTERESTS)
                .collect(Collectors.toMap(UserInterest::getTag, UserInterest::getWeight,
                        (a, b) -> a, LinkedHashMap::new));
        if (topK.isEmpty()) return Collections.emptyMap();

        double total = topK.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total == 0) return Collections.emptyMap();

        return topK.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() / total));
    }

    @Override
    @Transactional
    public void applyDecay(double decayFactor, double removalThreshold) {
        log.info("Applying interest decay: factor={}, threshold={}", decayFactor, removalThreshold);

        int decayed = userInterestRepository.decayAll(decayFactor);
        int removed = userInterestRepository.deleteBelowWeight(removalThreshold);

        log.info("Interest decay complete: {} rows decayed, {} rows removed", decayed, removed);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserInterest> getInterests(String userId) {
        return userInterestRepository.findByUserIdOrderByWeightDesc(userId);
    }
}
