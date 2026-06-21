package com.fsocial.postservice.services.impl;

import com.fsocial.postservice.entity.UserInterests;
import com.fsocial.postservice.repository.UserInterestsRepository;
import com.fsocial.postservice.services.InterestGraphService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterestGraphServiceImpl implements InterestGraphService {

    private final UserInterestsRepository userInterestsRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public void updateInterests(String userId, List<String> tags, double deltaWeight) {
        if (tags == null || tags.isEmpty() || deltaWeight == 0) return;

        LocalDateTime now = LocalDateTime.now();

        for (String tag : tags) {
            // Try to increment weight on existing tag entry
            Query matchExisting = new Query(
                Criteria.where("user_id").is(userId)
                        .and("interests.tag").is(tag)
            );
            Update incExisting = new Update()
                    .inc("interests.$.weight", deltaWeight)
                    .set("interests.$.updated_at", now)
                    .set("updated_at", now);

            var result = mongoTemplate.updateFirst(matchExisting, incExisting, UserInterests.class);

            if (result.getMatchedCount() == 0) {
                // Tag doesn't exist yet — push new entry (upsert the document too)
                Query matchUser = new Query(
                    Criteria.where("user_id").is(userId)
                            .and("interests.tag").ne(tag)
                );
                UserInterests.InterestItem newItem = UserInterests.InterestItem.builder()
                        .tag(tag)
                        .weight(Math.max(0, deltaWeight))
                        .updatedAt(now)
                        .build();
                Update pushNew = new Update()
                        .push("interests", newItem)
                        .set("updated_at", now)
                        .setOnInsert("user_id", userId);
                mongoTemplate.upsert(matchUser, pushNew, UserInterests.class);
            }
        }
        log.debug("Updated interests for user {} tags {} delta={}", userId, tags, deltaWeight);
    }

    @Override
    public Map<String, Double> getRawWeights(String userId) {
        return userInterestsRepository.findByUserId(userId)
                .map(ui -> ui.getInterests().stream()
                        .collect(Collectors.toMap(
                                UserInterests.InterestItem::getTag,
                                UserInterests.InterestItem::getWeight)))
                .orElse(Collections.emptyMap());
    }

    @Override
    public Map<String, Double> getNormalizedWeights(String userId) {
        Map<String, Double> raw = getRawWeights(userId);
        if (raw.isEmpty()) return Collections.emptyMap();

        double total = raw.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total == 0) return Collections.emptyMap();

        return raw.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue() / total));
    }

    @Override
    public void applyDecay(double decayFactor, double removalThreshold) {
        log.info("Applying interest decay: factor={}, threshold={}", decayFactor, removalThreshold);

        List<UserInterests> all = userInterestsRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        int decayed = 0;
        int removed = 0;

        for (UserInterests ui : all) {
            List<UserInterests.InterestItem> updated = new ArrayList<>();
            for (UserInterests.InterestItem item : ui.getInterests()) {
                double newWeight = item.getWeight() * decayFactor;
                if (newWeight >= removalThreshold) {
                    updated.add(UserInterests.InterestItem.builder()
                            .tag(item.getTag())
                            .weight(newWeight)
                            .updatedAt(now)
                            .build());
                    decayed++;
                } else {
                    removed++;
                }
            }
            ui.setInterests(updated);
            ui.setUpdatedAt(now);
            userInterestsRepository.save(ui);
        }

        log.info("Interest decay complete: {} decayed, {} removed below threshold", decayed, removed);
    }

    @Override
    public UserInterests getInterests(String userId) {
        return userInterestsRepository.findByUserId(userId).orElse(null);
    }
}
