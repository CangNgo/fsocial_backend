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

import org.bson.Document;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterestGraphServiceImpl implements InterestGraphService {

    private static final int TOP_K_INTERESTS = 10;

    private final UserInterestsRepository userInterestsRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public void updateInterests(String userId, List<String> tags, double deltaWeight) {
        if (tags == null || tags.isEmpty() || deltaWeight == 0) return;

        LocalDateTime now = LocalDateTime.now();

        // Step 1: Ensure document exists (setOnInsert only, no-op if already present)
        Query ensureDoc = new Query(Criteria.where("user_id").is(userId));
        Update upsertDoc = new Update()
                .setOnInsert("user_id", userId)
                .setOnInsert("interests", new ArrayList<>())
                .setOnInsert("updated_at", now);
        mongoTemplate.upsert(ensureDoc, upsertDoc, UserInterests.class);

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
                // Tag not present — push new entry; guard against concurrent push with ne(tag)
                Query matchNoTag = new Query(
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
                        .set("updated_at", now);
                var pushResult = mongoTemplate.updateFirst(matchNoTag, pushNew, UserInterests.class);
                if (pushResult.getMatchedCount() == 0) {
                    // Concurrent thread already pushed this tag — retry inc
                    mongoTemplate.updateFirst(matchExisting, incExisting, UserInterests.class);
                }
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

        // Take only top-K by weight to prevent affinity dilution
        Map<String, Double> topK = raw.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(TOP_K_INTERESTS)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));

        double total = topK.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total == 0) return Collections.emptyMap();

        return topK.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue() / total));
    }

    @Override
    public void applyDecay(double decayFactor, double removalThreshold) {
        log.info("Applying interest decay: factor={}, threshold={}", decayFactor, removalThreshold);

        // Single aggregation-pipeline updateMany — no data loaded into memory
        var collection = mongoTemplate.getCollection(
                mongoTemplate.getCollectionName(UserInterests.class));

        var pipeline = java.util.List.of(new Document("$set", new Document()
                .append("interests", new Document("$filter", new Document()
                        .append("input", new Document("$map", new Document()
                                .append("input", "$interests")
                                .append("in", new Document()
                                        .append("tag", "$$this.tag")
                                        .append("weight", new Document("$multiply",
                                                java.util.List.of("$$this.weight", decayFactor)))
                                        .append("updated_at", "$$NOW"))))
                        .append("cond", new Document("$gte",
                                java.util.List.of("$$this.weight", removalThreshold)))))
                .append("updated_at", "$$NOW")));

        var result = collection.updateMany(new Document(), pipeline);
        log.info("Interest decay complete: {} documents modified", result.getModifiedCount());
    }

    @Override
    public UserInterests getInterests(String userId) {
        return userInterestsRepository.findByUserId(userId).orElse(null);
    }
}
