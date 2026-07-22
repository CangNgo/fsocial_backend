package com.fsocial.postservice.services.impl;

import com.fsocial.postservice.dto.ActorSnapshotDTO;
import com.fsocial.postservice.dto.ContentDTO;
import com.fsocial.postservice.dto.notification.NotificationDTO;
import com.fsocial.postservice.dto.post.*;
import com.fsocial.postservice.entity.*;
import com.fsocial.postservice.enums.NotificationType;
import com.fsocial.postservice.exception.AppException;
import com.fsocial.postservice.exception.StatusCode;
import com.fsocial.postservice.mapper.ContentMapper;
import com.fsocial.postservice.mapper.PostMapper;
import com.fsocial.postservice.publisher.InteractionEventPublisher;
import com.fsocial.postservice.publisher.NotificationEvent;
import com.fsocial.postservice.repository.AccountRepository;
import com.fsocial.postservice.repository.CommentRepository;
import com.fsocial.postservice.repository.PostRepository;
import com.fsocial.postservice.dto.response.SearchPageResponse;
import com.fsocial.postservice.services.AccountService;
import com.fsocial.postservice.services.FeedService;
import com.fsocial.postservice.services.PostService;
import com.fsocial.postservice.services.RedisService;
import com.fsocial.postservice.util.DisplayNameUtils;
import com.fsocial.postservice.util.MediaUploadUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fsocial.postservice.util.PostUtils.buildPostResponse;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PostServiceImpl implements PostService {
    PostRepository postRepository;
    AccountRepository accountRepository;
    CommentRepository commentRepository;
    MongoTemplate mongoTemplate;
    MediaUploadUtils mediaUploadUtils;
    PostMapper postMapper;
    ContentMapper contentMapper;
    RedisService redisService;
    AccountService accountService;
    FeedService feedService;
    InteractionEventPublisher interactionEventPublisher;
    NotificationEvent notificationEvent;

    @Override
    @Transactional
    public PostDTO createPost(PostDTORequest postRequest) {
        MediaItem[] mediaItems = mediaUploadUtils.uploadValidMedia(postRequest.getMedia());
        try {
            ContentDTO contentDTO = buildContent(postRequest.getHtml(),
                    postRequest.getText(),
                    mediaItems);
            Post post = buildPost(contentDTO, postRequest);
            return postMapper.toPostDTO(postRepository.save(post));
        } catch (RuntimeException e) {
            log.error("Không thể thêm bài post vào database: {}", e.getMessage(), e);
            throw new AppException("Không thể thêm bài post vào database", StatusCode.CREATE_POST_FAILED);
        }
    }

    @Override
    public PostDTO updatePost(PostDTORequest post, String postId) {

        Post existingPost = postRepository.findById(postId)
                .orElseThrow(() -> new AppException("Post not found", StatusCode.POST_NOT_FOUND));
        //Nếu tìm thấy thì cập nhật thông tin

        postMapper.updateContent(post, existingPost.getContent());
        //cap nhat thoi gian
        existingPost.setUpdatedAt(LocalDateTime.now());
        return postMapper.toPostDTO(postRepository.save(existingPost));
    }

    @Override
    public void deletePost(String postId) {
        postRepository.deleteById(postId);
    }

    @Override
    public boolean toggleLike(String postId, String userId) throws Exception {
        boolean existed = postRepository.existsByIdAndLikes(postId, userId);
        try {
            if (!existed) {
                this.addLike(postId, userId);
                Post post = postRepository.findById(postId).orElseThrow();
                // Publish async score + interest update
                interactionEventPublisher.publish(postId, userId, "LIKE", post.getTags());
                notifyOwner(post.getOwner().getUserId(), userId, NotificationType.LIKE_SINGLE);
                return true;
            } else {
                this.removeLike(postId, userId);
                Post post = postRepository.findById(postId).orElseThrow();
                interactionEventPublisher.publish(postId, userId, "UNLIKE", post.getTags());
                return false;
            }
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public void addLike(String postId, String userId) {
        Query query = new Query(Criteria.where("_id").is(postId));
        Update update = new Update().addToSet("likes", userId);
        mongoTemplate.updateFirst(query, update, Post.class);
    }

    public void removeLike(String postId, String userId) {
        Query query = new Query(Criteria.where("_id").is(postId));
        Update update = new Update().pull("likes", userId);
        mongoTemplate.updateFirst(query, update, Post.class);
    }

    @Override
    public Integer CountLike(String postId, String userId) {
        Integer countLike = postRepository.countLikeByPost(postId);
        return countLike == null ? 0 : countLike;
    }

    @Override
    public PostDTO sharePost(PostShareDTORequest postRequest) {
        ContentDTO contentDTO = buildContent(postRequest.getHtml(), postRequest.getText());

        // Inherit tags from the origin post (BRD: share inherits post context)
        List<String> inheritedTags = postRepository.findById(postRequest.getOriginPostId())
                .map(Post::getTags)
                .orElse(new ArrayList<>());

        Post post = Post.builder()
                .content(contentMapper.toContent(contentDTO))
                .owner(ActorSnapshot.builder().userId(postRequest.getUserId()).build())
                .isShare(true)
                .originPostId(postRequest.getOriginPostId())
                .likes(new ArrayList<>())
                .tags(inheritedTags)
                .globalScore(0.0)
                .shareCount(0)
                .createDatetime(LocalDateTime.now())
                .build();

        redisService.personalization(postRequest.getUserId(), post.getOwner().getUserId());
        Post saved = postRepository.save(post);

        // Publish SHARE event → score update on origin post + interest update for user
        interactionEventPublisher.publish(postRequest.getOriginPostId(),
                postRequest.getUserId(), "SHARE", inheritedTags);

        postRepository.findById(postRequest.getOriginPostId()).ifPresent(origin ->
                notifyOwner(origin.getOwner().getUserId(), postRequest.getUserId(), NotificationType.SHARE));

        return postMapper.toPostDTO(saved);
    }

    /** Bỏ qua nếu tự thao tác trên bài viết của chính mình */
    private void notifyOwner(String ownerId, String actorId, NotificationType type) {
        if (ownerId == null || ownerId.equals(actorId)) return;
        notificationEvent.publishCreateNotification(new NotificationDTO(ownerId, actorId, type));
    }

    private ContentDTO buildContent(String html, String text, MediaItem[] media) {
        return ContentDTO.builder()
                .text(text)
                .html(html)
                .media(media != null && media.length > 0 ? Arrays.asList(media) : null)
                .build();
    }

    private ContentDTO buildContent(String html, String text) {
        return ContentDTO.builder()
                .text(text)
                .html(html)
                .build();
    }

    private Post buildPost(ContentDTO contentDTO, PostDTORequest postRequest) {
        Post post = postMapper.toPost(postRequest);

        ActorSnapshotDTO owner = accountService.getOwner(postRequest.getUserId());
        post.setOwner(postMapper.toActorSnapshot(owner));
        post.setContent(contentMapper.toContent(contentDTO));
        post.setCreateDatetime(LocalDateTime.now());
        post.setLikes(new ArrayList<>());
        // Tags from request (BRD)
        post.setTags(postRequest.getTags() != null ? postRequest.getTags() : new ArrayList<>());
        post.setGlobalScore(0.0);
        post.setShareCount(0);
        return post;
    }

    @Override
    public List<Post> getPostsByUser(String userId, String requesterId) {
        Account owner = accountRepository.findById(userId).orElse(null);

        if (owner == null || Boolean.TRUE.equals(owner.getIsPublic())) {
            return postRepository.findByOwnerUserId(userId);
        }

        if (owner.getFollower().contains(requesterId)) {
            return postRepository.findByOwnerUserId(userId);
        }

        return Collections.emptyList();
    }

    @Override
    public List<PostResponse> getPostsByUserId(String userId, int feedSize) {
        List<PostResponse> feed = feedService.buildPersonalizedFeed(userId, feedSize);
        log.info("Lấy bài viết thành công, count={}", feed.size());
        return feed;
    }

    @Override
    public List<PostStatisticsLongDateDTO> countStatisticsPostLongDay(LocalDateTime startDate, LocalDateTime endDate) {
        List<PostStatisticsLongDateDTO> postStatisticsDTOS = postRepository.countByDate(startDate, endDate);
        List<PostStatisticsLongDateDTO> result = new ArrayList<>();
        LocalDateTime start = startDate.truncatedTo(ChronoUnit.DAYS);
        LocalDateTime end = endDate.plusDays(1).truncatedTo(ChronoUnit.DAYS);
        while (!start.equals(end)) {
            for (PostStatisticsLongDateDTO complaint : postStatisticsDTOS) {
                if (complaint.getDate().truncatedTo(ChronoUnit.DAYS).equals(start)) {
                    result.add(complaint);
                } else {
                    result.add(new PostStatisticsLongDateDTO(start, 0));
                }
            }

            if (postStatisticsDTOS.isEmpty()) {
                result.add(new PostStatisticsLongDateDTO(start, 0));
            }
            start = start.plusDays(1);
        }
        return result;
    }

    @Override
    public List<PostStatisticsDTO> countStatisticsPostToday(LocalDateTime startDate, LocalDateTime endDate) {
        List<PostStatisticsDTO> rawData = postRepository.countByCreatedAtByHours(startDate, endDate);
        Map<String, Integer> countByHour = rawData.stream()
                .collect(Collectors.toMap(PostStatisticsDTO::getHour, PostStatisticsDTO::getCount));

        List<PostStatisticsDTO> result = new ArrayList<>(24);
        for (int hour = 0; hour < 24; hour++) {
            String key = String.valueOf(hour);
            result.add(new PostStatisticsDTO(key, countByHour.getOrDefault(key, 0)));
        }
        return result;
    }

    @Override
    public SearchPageResponse<PostResponse> findByText(String text, String userId, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(50, size);
        String trimmedText = text == null ? "" : text.trim();

        if (trimmedText.isBlank()) {
            return new SearchPageResponse<>(List.of(), safePage, safeSize, false);
        }

        Pageable pageable = PageRequest.of(safePage, safeSize + 1);
        List<Post> posts = postRepository.findByContentTextContainingIgnoreCase(trimmedText, pageable);
        boolean hasMore = posts.size() > safeSize;
        List<PostResponse> items = toPostResponses(posts.stream().limit(safeSize).toList(), userId);

        return new SearchPageResponse<>(items, safePage, safeSize, hasMore);
    }

    @Override
    public PostResponse getPostById(String postId, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException("Không tìm thấy thông tin bài viết", StatusCode.POST_NOT_FOUND));
        Account owner = accountRepository.findById(post.getOwner().getUserId())
                .orElseThrow(() -> new AppException("Không tìm thấy chủ bài viết", StatusCode.POST_NOT_FOUND));
        Integer commentCount = commentRepository.countByPostId(post.getId());
        return buildPostResponse(post, owner, commentCount == null ? 0 : commentCount, userId);
    }

    @Override
    public List<PostResponse> getPostByFollowing(String userId) {
        Pageable pageable = PageRequest.of(0, 10);

        Account account = accountRepository.findById(userId).orElse(null);
        List<String> followingIds = account != null
                ? new ArrayList<>(account.getFollowing())
                : List.of();
        if (followingIds.isEmpty()) {
            return List.of();
        }

        List<String> viewed = redisService.getViewedFollowing(userId);
        List<Post> result = postRepository.findByOwnerUserIdInAndIdNotInOrderByCreateDatetimeDesc(
                followingIds, viewed, pageable);

        result.forEach(p -> redisService.viewedFollowing(userId, p.getId()));
        return toPostResponses(result, userId);
    }

    private List<PostResponse> toPostResponses(List<Post> posts, String requesterId) {
        if (posts.isEmpty()) return List.of();

        List<String> postIds = posts.stream().map(Post::getId).toList();
        List<String> ownerIds = posts.stream()
                .map(p -> p.getOwner().getUserId())
                .distinct().toList();

        Map<String, Account> accountMap = accountRepository.findAllById(ownerIds).stream()
                .collect(Collectors.toMap(Account::getId, Function.identity()));

        Map<String, Integer> commentCountMap = commentRepository.countByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(CommentRepository.PostCommentCount::_id,
                        CommentRepository.PostCommentCount::count));

        return posts.stream()
                .map(post -> {
                    String ownerId = post.getOwner().getUserId();
                    Account owner = accountMap.get(ownerId);
                    if (owner == null) {
                        log.warn("Bỏ qua post {} vì không tìm thấy account {}", post.getId(), ownerId);
                        return null;
                    }
                    return buildPostResponse(post, owner,
                            commentCountMap.get(post.getId()), requesterId);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
