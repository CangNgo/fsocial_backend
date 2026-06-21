package com.fsocial.postservice.services;

import com.fsocial.postservice.entity.UserInterests;

import java.util.List;
import java.util.Map;

public interface InterestGraphService {

    /**
     * Atomically add deltaWeight to each tag for userId.
     * Creates the document and/or tag entries if they don't exist.
     */
    void updateInterests(String userId, List<String> tags, double deltaWeight);

    /**
     * Returns tag→weight map for userId, empty map if no interests found.
     */
    Map<String, Double> getRawWeights(String userId);

    /**
     * Returns tag→normalizedWeight where all weights are scaled to sum = 1.0.
     * Returns empty map if no interests found.
     */
    Map<String, Double> getNormalizedWeights(String userId);

    /**
     * Applies decay: weight = weight × decayFactor for all users.
     * Removes entries with weight < threshold.
     */
    void applyDecay(double decayFactor, double removalThreshold);

    UserInterests getInterests(String userId);
}
