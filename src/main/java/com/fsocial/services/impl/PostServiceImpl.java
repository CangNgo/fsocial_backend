package com.fsocial.services.impl;

import com.fsocial.dto.notification.NotificationDTO;
import com.fsocial.dto.post.*;
import com.fsocial.entity.Account;
import com.fsocial.entity.Post;
import com.fsocial.entity.PostLike;
import com.fsocial.dto.post.*;
import com.fsocial.dto.response.SearchPageResponse;
import com.fsocial.entity.*;
import com.fsocial.enums.AttachmentType;
import com.fsocial.enums.NotificationType;
import com.fsocial.exception.AppException;
import com.fsocial.exception.StatusCode;
import com.fsocial.mapper.PostMapper;
import com.fsocial.publisher.InteractionEventPublisher;
import com.fsocial.publisher.NotificationEvent;
import com.fsocial.repository.*;
import com.fsocial.services.AttachmentsService;
import com.fsocial.services.FeedService;
import com.fsocial.services.PostService;
import com.fsocial.services.RedisService;
import com.fsocial.util.MediaUploadUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fsocial.util.PostUtils.buildPostResponse;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PostServiceImpl implements PostService {
    PostRepository postRepository;
    PostLikeRepository postLikeRepository;
    AccountRepository accountRepository;
    CommentRepository commentRepository;
    FollowRepository followRepository;
    MediaUploadUtils mediaUploadUtils;
    PostMapper postMapper;
    RedisService redisService;
    FeedService feedService;
    InteractionEventPublisher interactionEventPublisher;
    NotificationEvent notificationEvent;
    AttachmentsService attachmentsService;

    @Override
    @Transactional
    public PostDTO createPost(PostDTORequest postRequest) {
        MediaItemDTO[] mediaItems = mediaUploadUtils.uploadValidMedia(postRequest.getMedia(), AttachmentType.POST);
        Account owner = accountRepository.findById(postRequest.getUserId())
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng", StatusCode.USER_NOT_FOUND));
        try {
            Post post = Post.builder()
                    .owner(owner)
                    .text(postRequest.getText())
                    .html(postRequest.getHtml())
                    .createDatetime(LocalDateTime.now())
                    .tags(normalizeTags(postRequest.getTags()))
                    .build();

            Post saved = postRepository.save(post);
            linkMedia(saved, mediaItems);
            return postMapper.toPostDTO(saved);
        } catch (RuntimeException e) {
            log.error("Không thể thêm bài post vào database: {}", e.getMessage(), e);
            throw new AppException("Không thể thêm bài post vào database", StatusCode.CREATE_POST_FAILED);
        }
    }

    @Override
    @Transactional
    public PostDTO updatePost(PostDTORequest post, String postId) {
        Post existingPost = postRepository.findById(postId)
                .orElseThrow(() -> new AppException("Post not found", StatusCode.POST_NOT_FOUND));

        existingPost.setText(post.getText());
        existingPost.setHtml(post.getHtml());
        existingPost.setUpdatedAt(LocalDateTime.now());
        return postMapper.toPostDTO(postRepository.save(existingPost));
    }

    @Override
    public void deletePost(String postId) {
        postRepository.deleteById(postId);
    }

    @Override
    @Transactional
    public boolean toggleLike(String postId, String userId) throws Exception {
        boolean existed = postLikeRepository.existsByPostIdAndUserId(postId, userId);
        try {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new AppException("Không tìm thấy bài viết", StatusCode.POST_NOT_FOUND));
            List<String> tags = post.getTags();

            if (!existed) {
                addLike(postId, userId);
                interactionEventPublisher.publish(postId, userId, "LIKE", tags);
                notifyOwner(post.getOwner().getId(), userId, NotificationType.LIKE_SINGLE);
                return true;
            }
            removeLike(postId, userId);
            interactionEventPublisher.publish(postId, userId, "UNLIKE", tags);
            return false;
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public void addLike(String postId, String userId) {
        // PK (post_id, user_id) đảm bảo idempotent; existsBy đã chặn trước nên save là insert.
        postLikeRepository.save(new PostLike(postId, userId));
    }

    public void removeLike(String postId, String userId) {
        postLikeRepository.deleteByPostIdAndUserId(postId, userId);
    }

    @Override
    public Integer CountLike(String postId, String userId) {
        return postLikeRepository.countByPostId(postId);
    }

    @Override
    @Transactional
    public PostDTO sharePost(PostShareDTORequest postRequest) {
        Post origin = postRepository.findById(postRequest.getOriginPostId())
                .orElseThrow(() -> new AppException("Không tìm thấy bài viết gốc", StatusCode.POST_NOT_FOUND));
        Account owner = accountRepository.findById(postRequest.getUserId())
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng", StatusCode.USER_NOT_FOUND));

        Post post = Post.builder()
                .owner(owner)
                .text(postRequest.getText())
                .html(postRequest.getHtml())
                .isShare(true)
                .originPostId(postRequest.getOriginPostId())
                .createDatetime(LocalDateTime.now())
                .tags(normalizeTags(origin.getTags()))
                .build();

        Post saved = postRepository.save(post);

        redisService.personalization(postRequest.getUserId(), owner.getId());

        // SHARE event → cập nhật score bài gốc + interest của user
        interactionEventPublisher.publish(postRequest.getOriginPostId(),
                postRequest.getUserId(), "SHARE", saved.getTags());
        notifyOwner(origin.getOwner().getId(), postRequest.getUserId(), NotificationType.SHARE);

        return postMapper.toPostDTO(saved);
    }

    /** Bỏ qua nếu tự thao tác trên bài viết của chính mình */
    private void notifyOwner(String ownerId, String actorId, NotificationType type) {
        if (ownerId == null || ownerId.equals(actorId)) return;
        notificationEvent.publishCreateNotification(new NotificationDTO(ownerId, actorId, type));
    }

    private void linkMedia(Post post, MediaItemDTO[] mediaItems) {
        if (mediaItems == null) return;
        for (int i = 0; i < mediaItems.length; i++) {
            MediaItemDTO item = mediaItems[i];
            if (item == null || item.getAttachmentId() == null) continue;
            post.addMedia(attachmentsService.linkToPost(item.getAttachmentId(), post, i));
        }
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) return List.of();
        return tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .distinct()
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPostsByUser(String userId, String requesterId) {
        Account owner = accountRepository.findById(userId).orElse(null);

        boolean visible = owner == null
                || Boolean.TRUE.equals(owner.getIsPublic())
                || userId.equals(requesterId)
                || followRepository.existsByFollowerIdAndFolloweeId(requesterId, userId);

        if (!visible) return List.of();
        return toPostResponses(postRepository.findByOwnerId(userId), requesterId);
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
    @Transactional(readOnly = true)
    public SearchPageResponse<PostResponse> findByText(String text, String userId, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(50, size);
        String trimmedText = text == null ? "" : text.trim();

        if (trimmedText.isBlank()) {
            return new SearchPageResponse<>(List.of(), safePage, safeSize, false);
        }

        Pageable pageable = PageRequest.of(safePage, safeSize + 1);
        List<Post> posts = postRepository.findByTextContainingIgnoreCase(trimmedText, pageable);
        boolean hasMore = posts.size() > safeSize;
        List<PostResponse> items = toPostResponses(posts.stream().limit(safeSize).toList(), userId);

        return new SearchPageResponse<>(items, safePage, safeSize, hasMore);
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getPostById(String postId, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException("Không tìm thấy thông tin bài viết", StatusCode.POST_NOT_FOUND));
        Account owner = accountRepository.findById(post.getOwner().getId())
                .orElseThrow(() -> new AppException("Không tìm thấy chủ bài viết", StatusCode.POST_NOT_FOUND));
        Integer commentCount = commentRepository.countByPostId(postId);
        return buildPostResponse(post, owner,
                commentCount == null ? 0 : commentCount,
                postLikeRepository.countByPostId(postId),
                postLikeRepository.existsByPostIdAndUserId(postId, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> getPostByFollowing(String userId) {
        Pageable pageable = PageRequest.of(0, 10);

        List<String> followingIds = followRepository.findFolloweeIds(userId);
        if (followingIds.isEmpty()) {
            return List.of();
        }

        List<String> viewed = redisService.getViewedFollowing(userId);
        List<Post> result = postRepository.findByOwnerIdInAndIdNotInOrderByCreateDatetimeDesc(
                followingIds, excludable(viewed), pageable);

        result.forEach(p -> redisService.viewedFollowing(userId, p.getId()));
        return toPostResponses(result, userId);
    }

    /** JPQL {@code not in ()} rỗng là cú pháp lỗi — luôn có ít nhất 1 phần tử không tồn tại. */
    private List<String> excludable(List<String> ids) {
        return (ids == null || ids.isEmpty()) ? List.of("") : ids;
    }

    private List<PostResponse> toPostResponses(List<Post> posts, String requesterId) {
        if (posts.isEmpty()) return List.of();

        List<String> postIds = posts.stream().map(Post::getId).toList();
        List<String> ownerIds = posts.stream()
                .map(p -> p.getOwner().getId())
                .distinct().toList();

        Map<String, Account> accountMap = accountRepository.findAllById(ownerIds).stream()
                .collect(Collectors.toMap(Account::getId, Function.identity()));

        Map<String, Integer> commentCountMap = commentRepository.countByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(CommentRepository.PostCommentCount::_id,
                        CommentRepository.PostCommentCount::count));

        Map<String, Integer> likeCountMap = postLikeRepository.countByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> ((Number) r[1]).intValue()));

        Set<String> likedByRequester = new HashSet<>(
                postLikeRepository.findLikedPostIds(requesterId, postIds));

        return posts.stream()
                .map(post -> {
                    String ownerId = post.getOwner().getId();
                    Account owner = accountMap.get(ownerId);
                    if (owner == null) {
                        log.warn("Bỏ qua post {} vì không tìm thấy account {}", post.getId(), ownerId);
                        return null;
                    }
                    return buildPostResponse(post, owner,
                            commentCountMap.getOrDefault(post.getId(), 0),
                            likeCountMap.getOrDefault(post.getId(), 0),
                            likedByRequester.contains(post.getId()));
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostResponse> getMyPost(String userId, String lastItem, String createdAt) {

        Sort sort = Sort.by("createdAt").descending().and(Sort.by("id").descending());
        ScrollPosition position;
        if(lastItem.isEmpty()) {
            position = ScrollPosition.keyset();
        }else {
            position = ScrollPosition.forward(Map.of("createdAt", createdAt, "id", lastItem));
        }

//        Window<PostResponse> data = postRepository.findByCreatedBy();

        return List.of();
    }
}
