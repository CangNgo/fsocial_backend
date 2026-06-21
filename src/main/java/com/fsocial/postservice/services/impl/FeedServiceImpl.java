package com.fsocial.postservice.services.impl;

import com.fsocial.postservice.dto.post.PostResponse;
import com.fsocial.postservice.entity.*;
import com.fsocial.postservice.repository.*;
import com.fsocial.postservice.services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedServiceImpl implements FeedService {

    private static final double EXPLOIT_RATIO = 0.70;
    private static final double EXPLORE_RATIO = 0.20;
    // wildcard fills the remainder: 1.0 - 0.70 - 0.20 = 0.10

    private static final int MAX_CANDIDATE_POOL_PER_TAG = 100;
    private static final int MAX_RELATED_TAGS = 5;

    private final PostRepository postRepository;
    private final SeenPostRepository seenPostRepository;
    private final AccountRepository accountRepository;
    private final CommentRepository commentRepository;
    private final RelationshipRepository relationshipRepository;
    private final TagCooccurrenceRepository tagCooccurrenceRepository;
    private final InterestGraphService interestGraphService;
    private final ScoringService scoringService;
    private final MongoTemplate mongoTemplate;

    @Override
    public List<PostResponse> buildPersonalizedFeed(String userId, int feedSize) {
        Map<String, Double> normalizedWeights = interestGraphService.getNormalizedWeights(userId);
        List<String> seenIds = getSeenPostIds(userId);

        List<Post> candidates;
        if (normalizedWeights.isEmpty()) {
            // Cold start: fall back to chronological feed (no interest data)
            candidates = postRepository.findByIdNotInOrderByCreateDatetimeDesc(
                    seenIds.isEmpty() ? List.of() : seenIds,
                    PageRequest.of(0, feedSize));
        } else {
            candidates = buildCandidatePool(userId, normalizedWeights, seenIds, feedSize);
        }

        if (candidates.isEmpty()) {
            // All posts seen — reset and return fresh chronological batch
            seenPostRepository.deleteByUserId(userId);
            candidates = postRepository.findByIdNotInOrderByCreateDatetimeDesc(
                    List.of(), PageRequest.of(0, feedSize));
        }

        // Enrich with social context and sort by final_score
        Set<String> followingIds = getFollowingIds(userId);
        Map<String, Integer> commentCountMap = buildCommentCountMap(candidates);

        candidates.sort((p1, p2) -> {
            double s1 = scoringService.calculateFinalScore(p1,
                    commentCountMap.getOrDefault(p1.getId(), 0),
                    normalizedWeights,
                    followingIds.contains(p1.getOwner().getUserId()));
            double s2 = scoringService.calculateFinalScore(p2,
                    commentCountMap.getOrDefault(p2.getId(), 0),
                    normalizedWeights,
                    followingIds.contains(p2.getOwner().getUserId()));
            return Double.compare(s2, s1);
        });

        // Mark posts as seen
        candidates.forEach(p -> markSeen(userId, p.getId()));

        return toPostResponses(candidates, userId);
    }

    private List<Post> buildCandidatePool(String userId, Map<String, Double> normalizedWeights,
                                           List<String> seenIds, int feedSize) {
        int exploitSize = (int) Math.round(feedSize * EXPLOIT_RATIO);
        int exploreSize = (int) Math.round(feedSize * EXPLORE_RATIO);
        int wildcardSize = feedSize - exploitSize - exploreSize;

        List<Post> exploitPosts = getExploitPosts(normalizedWeights, seenIds, exploitSize);
        Set<String> alreadyChosen = exploitPosts.stream().map(Post::getId).collect(Collectors.toSet());

        List<String> exploredSeenIds = new ArrayList<>(seenIds);
        exploredSeenIds.addAll(alreadyChosen);
        List<Post> explorePosts = getExplorePosts(normalizedWeights, exploredSeenIds, exploreSize);
        alreadyChosen.addAll(explorePosts.stream().map(Post::getId).collect(Collectors.toSet()));

        List<String> wildcardSeenIds = new ArrayList<>(seenIds);
        wildcardSeenIds.addAll(alreadyChosen);
        List<Post> wildcardPosts = getWildcardPosts(wildcardSeenIds, wildcardSize);

        List<Post> merged = new ArrayList<>();
        merged.addAll(exploitPosts);
        merged.addAll(explorePosts);
        merged.addAll(wildcardPosts);
        return merged;
    }

    /**
     * Exploit (70%): Budget allocation per tag, weighted sampling within each pool.
     */
    private List<Post> getExploitPosts(Map<String, Double> normalizedWeights,
                                        List<String> seenIds, int totalSlots) {
        List<Post> result = new ArrayList<>();
        List<String> exclusions = seenIds.isEmpty() ? List.of() : seenIds;

        for (Map.Entry<String, Double> entry : normalizedWeights.entrySet()) {
            int slots = Math.max(1, (int) Math.round(entry.getValue() * totalSlots));
            int poolSize = Math.min(MAX_CANDIDATE_POOL_PER_TAG, slots * 5);

            List<Post> pool = postRepository.findByTagAndIdNotIn(
                    entry.getKey(), exclusions, PageRequest.of(0, poolSize));

            List<Post> sampled = weightedSample(pool, slots);
            result.addAll(sampled);
        }

        return result.stream()
                .collect(Collectors.toMap(Post::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new))
                .values().stream()
                .limit(totalSlots)
                .collect(Collectors.toList());
    }

    /**
     * Explore (20%): Use tag co-occurrence to find related tags user hasn't explicitly expressed.
     */
    private List<Post> getExplorePosts(Map<String, Double> normalizedWeights,
                                        List<String> seenIds, int totalSlots) {
        List<String> knownTags = new ArrayList<>(normalizedWeights.keySet());
        Set<String> relatedTags = new LinkedHashSet<>();

        for (String tag : knownTags) {
            tagCooccurrenceRepository.findByTagAOrderByCountDesc(tag, PageRequest.of(0, MAX_RELATED_TAGS))
                    .stream()
                    .map(TagCooccurrence::getTagB)
                    .filter(t -> !knownTags.contains(t))
                    .forEach(relatedTags::add);
            if (relatedTags.size() >= MAX_RELATED_TAGS * 2) break;
        }

        if (relatedTags.isEmpty()) {
            // No co-occurrence data yet — fallback: use posts from tags user hasn't seen much
            return postRepository.findByTagsInAndIdNotIn(knownTags, seenIds, PageRequest.of(0, totalSlots));
        }

        List<String> exclusions = seenIds.isEmpty() ? List.of() : seenIds;
        return postRepository.findByTagsInAndIdNotIn(
                new ArrayList<>(relatedTags), exclusions, PageRequest.of(0, totalSlots));
    }

    /**
     * Wildcard (10%): Top globally-scored posts the user hasn't seen.
     */
    private List<Post> getWildcardPosts(List<String> seenIds, int totalSlots) {
        List<String> exclusions = seenIds.isEmpty() ? List.of() : seenIds;
        return postRepository.findTopByGlobalScore(exclusions, PageRequest.of(0, totalSlots));
    }

    /**
     * Weighted random sampling without replacement from a candidate pool.
     * All posts in pool have equal probability (globalScore already used for ordering by DB).
     */
    private List<Post> weightedSample(List<Post> pool, int count) {
        if (pool.size() <= count) return new ArrayList<>(pool);
        Collections.shuffle(pool);
        return new ArrayList<>(pool.subList(0, count));
    }

    @Override
    public void markSeen(String userId, String postId) {
        try {
            Query query = new Query(Criteria.where("user_id").is(userId).and("post_id").is(postId));
            Update update = new Update()
                    .set("user_id", userId)
                    .set("post_id", postId)
                    .set("seen_at", LocalDateTime.now());
            mongoTemplate.upsert(query, update, SeenPost.class);
        } catch (Exception e) {
            log.warn("Failed to mark post {} as seen for user {}: {}", postId, userId, e.getMessage());
        }
    }

    @Override
    public List<String> getSeenPostIds(String userId) {
        return seenPostRepository.findByUserId(userId)
                .stream()
                .map(SeenPost::getPostId)
                .collect(Collectors.toList());
    }

    private Set<String> getFollowingIds(String userId) {
        return relationshipRepository.findByUserId(userId)
                .map(Relationship::getListFollowing)
                .orElse(Collections.emptySet());
    }

    private Map<String, Integer> buildCommentCountMap(List<Post> posts) {
        if (posts.isEmpty()) return Collections.emptyMap();
        List<String> postIds = posts.stream().map(Post::getId).toList();
        return commentRepository.countByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(
                        CommentRepository.PostCommentCount::_id,
                        CommentRepository.PostCommentCount::count));
    }

    private List<PostResponse> toPostResponses(List<Post> posts, String requesterId) {
        if (posts.isEmpty()) return List.of();

        List<String> ownerIds = posts.stream()
                .map(p -> p.getOwner().getUserId())
                .distinct().toList();

        Map<String, Account> accountMap = accountRepository.findAllById(ownerIds).stream()
                .collect(Collectors.toMap(Account::getId, Function.identity()));

        Map<String, Integer> commentCountMap = buildCommentCountMap(posts);

        return posts.stream()
                .map(post -> {
                    Account owner = accountMap.get(post.getOwner().getUserId());
                    if (owner == null) {
                        log.warn("Owner not found for post {}", post.getId());
                        return null;
                    }
                    return PostResponse.builder()
                            .id(post.getId())
                            .originPostId(post.getOriginPostId())
                            .content(post.getContent())
                            .countLikes(post.getLikes() == null ? 0 : post.getLikes().size())
                            .countComments(commentCountMap.getOrDefault(post.getId(), 0))
                            .userId(post.getOwner().getUserId())
                            .displayName(owner.getLastName().concat(" " + owner.getFirstName()))
                            .avatar(owner.getAvatar())
                            .createDatetime(post.getCreateDatetime())
                            .isLike(post.getLikes() != null && post.getLikes().contains(requesterId))
                            .isShare(Boolean.TRUE.equals(post.getIsShare()))
                            .status(Boolean.TRUE.equals(post.getStatus()))
                            .tags(post.getTags())
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
