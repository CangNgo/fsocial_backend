package com.fsocial.services.impl;

import com.fsocial.dto.comment.CommentDTO;
import com.fsocial.dto.comment.CommentDTORequest;
import com.fsocial.dto.comment.CommentResponse;
import com.fsocial.dto.comment.CommentUpdateDTORequest;
import com.fsocial.dto.notification.NotificationDTO;
import com.fsocial.dto.post.ContentResponse;
import com.fsocial.dto.post.MediaItemDTO;
import com.fsocial.entity.Account;
import com.fsocial.entity.Comment;
import com.fsocial.entity.CommentLike;
import com.fsocial.entity.CommentMedia;
import com.fsocial.entity.Post;
import com.fsocial.enums.NotificationType;
import com.fsocial.exception.AppException;
import com.fsocial.exception.StatusCode;
import com.fsocial.publisher.InteractionEventPublisher;
import com.fsocial.publisher.NotificationEvent;
import com.fsocial.repository.AccountRepository;
import com.fsocial.repository.CommentLikeRepository;
import com.fsocial.repository.CommentRepository;
import com.fsocial.repository.PostRepository;
import com.fsocial.services.AccountService;
import com.fsocial.services.CommentService;
import com.fsocial.services.RedisService;
import com.fsocial.util.DisplayNameUtils;
import com.fsocial.util.MediaUploadUtils;
import com.fsocial.util.PostUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CommentServiceImpl implements CommentService {
    CommentRepository commentRepository;
    CommentLikeRepository commentLikeRepository;
    MediaUploadUtils mediaUploadUtils;
    PostRepository postRepository;
    AccountService accountService;
    AccountRepository accountRepository;
    RedisService redisService;
    InteractionEventPublisher interactionEventPublisher;
    NotificationEvent notificationEvent;

    @Override
    @Transactional
    public CommentResponse addComment(CommentDTORequest request) {
        MediaItemDTO[] mediaItems = mediaUploadUtils.uploadValidMedia(request.getMedia());

        String postId = request.getPostId();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException("Không tìm thấy bài đăng", StatusCode.POST_NOT_FOUND));

        Comment comment = Comment.builder()
                .postId(postId)
                .userId(request.getUserId())
                .text(request.getText())
                .html(request.getHtml())
                .createDatetime(LocalDateTime.now())
                .build();
        attachMedia(comment, mediaItems);
        Comment saved = commentRepository.save(comment);

        String ownerId = post.getOwner().getId();
        redisService.personalization(saved.getUserId(), ownerId);

        // Publish async COMMENT event for score + interest update
        interactionEventPublisher.publish(postId, request.getUserId(), "COMMENT", post.getTags());

        notifyOwner(ownerId, request.getUserId(), NotificationType.COMMENT_SINGLE);

        Account owner = accountRepository.findById(saved.getUserId()).orElse(null);
        return buildCommentResponse(saved, owner, 0, 0, false);
    }

    /** Bỏ qua nếu tự bình luận trên bài viết của chính mình */
    private void notifyOwner(String ownerId, String actorId, NotificationType type) {
        if (ownerId == null || ownerId.equals(actorId)) return;
        notificationEvent.publishCreateNotification(new NotificationDTO(ownerId, actorId, type));
    }

    private void attachMedia(Comment comment, MediaItemDTO[] mediaItems) {
        if (mediaItems == null) return;
        for (MediaItemDTO item : mediaItems) {
            if (item == null) continue;
            comment.addMedia(CommentMedia.builder()
                    .url(item.getUrl())
                    .type(item.getMediaType())
                    .width(item.getWidth())
                    .height(item.getHeight())
                    .build());
        }
    }

    @Override
    @Transactional
    public boolean toggleLikeComment(String commentId, String userId) {
        if (!commentExist(commentId))
            throw new AppException("Bình luận không tồn tại", StatusCode.COMMENT_NOT_FOUND);
        if (!userExists(userId))
            throw new AppException("Tài khoản người dùng không tồn tại", StatusCode.USER_NOT_FOUND);

        if (!commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)) {
            // PK (comment_id, user_id) đảm bảo idempotent
            commentLikeRepository.save(new CommentLike(commentId, userId));
            return true;
        }
        commentLikeRepository.deleteByCommentIdAndUserId(commentId, userId);
        return false;
    }

    @Override
    public Integer countLike(String commentId, String userId) {
        return commentLikeRepository.countByCommentId(commentId);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(CommentUpdateDTORequest request) {
        if (!userExists(request.getUserId()))
            throw new AppException("User không tồn tại", StatusCode.USER_NOT_FOUND);

        Comment comment = commentRepository.findById(request.getCommentId())
                .orElseThrow(() -> new AppException("Không tìm thấy comment", StatusCode.COMMENT_NOT_FOUND));
        comment.setText(request.getText());
        comment.setHtml(request.getHtml());
        return convertToCommentResponse(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public String deleteComment(String commentID) {
        commentRepository.findById(commentID).ifPresent(comment -> {
            // FK comment.parent_id ON DELETE CASCADE dọn reply; comment_like/comment_media cascade theo
            commentRepository.deleteById(commentID);
            // Publish COMMENT_DELETE so score + interest are recomputed
            interactionEventPublisher.publish(comment.getPostId(), comment.getUserId(), "COMMENT_DELETE",
                    postRepository.findById(comment.getPostId()).map(Post::getTags).orElse(List.of()));
        });
        return "Xóa comment thành công";
    }

    public boolean userExists(String userId) {
        return accountService.existsById(userId);
    }

    public boolean commentExist(String commentId) {
        return commentRepository.existsById(commentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(String postId) {
        return toCommentResponses(commentRepository.findByPostId(postId), currentUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponse convertToCommentResponse(Comment comment) {
        Account owner = accountRepository.findById(comment.getUserId()).orElse(null);
        String requesterId = currentUserId();
        return buildCommentResponse(comment, owner,
                commentLikeRepository.countByCommentId(comment.getId()),
                commentRepository.findByParentId(comment.getId()).size(),
                requesterId != null
                        && commentLikeRepository.existsByCommentIdAndUserId(comment.getId(), requesterId));
    }

    @Override
    @Transactional
    public List<CommentDTO> deleteCommentByPostId(String postId) {
        commentRepository.deleteByPostId(postId);
        return List.of();
    }

    private List<CommentResponse> toCommentResponses(List<Comment> comments, String requesterId) {
        if (comments.isEmpty()) return List.of();

        List<String> commentIds = comments.stream().map(Comment::getId).toList();
        List<String> ownerIds = comments.stream().map(Comment::getUserId).distinct().toList();

        Map<String, Account> accountMap = accountRepository.findAllById(ownerIds).stream()
                .collect(Collectors.toMap(Account::getId, Function.identity()));

        Map<String, Integer> likeCountMap = commentLikeRepository.countByCommentIdIn(commentIds).stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> ((Number) r[1]).intValue()));

        Map<String, Integer> replyCountMap = commentRepository.countByParentIdIn(commentIds).stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> ((Number) r[1]).intValue()));

        Set<String> liked = requesterId == null
                ? Set.of()
                : new HashSet<>(commentLikeRepository.findLikedCommentIds(requesterId, commentIds));

        return comments.stream()
                .map(c -> buildCommentResponse(c, accountMap.get(c.getUserId()),
                        likeCountMap.getOrDefault(c.getId(), 0),
                        replyCountMap.getOrDefault(c.getId(), 0),
                        liked.contains(c.getId())))
                .collect(Collectors.toList());
    }

    private CommentResponse buildCommentResponse(Comment comment, Account owner,
                                                 int likeCount, int replyCount, boolean liked) {
        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .content(buildContent(comment))
                .countLikes(likeCount)
                .countReplies(replyCount)
                .displayName(DisplayNameUtils.build(owner))
                .userId(comment.getUserId())
                .reply(comment.isReply())
                .like(liked)
                .avatar(owner == null ? null : owner.getAvatar())
                .createDatetime(comment.getCreateDatetime())
                .build();
    }

    static ContentResponse buildContent(Comment comment) {
        List<CommentMedia> media = comment.getMedia();
        return ContentResponse.builder()
                .text(comment.getText())
                .html(comment.getHtml())
                .media(media == null ? List.of() : media.stream()
                        .filter(Objects::nonNull)
                        .map(m -> PostUtils.toMediaResponse(m.getType(), m.getUrl(), m.getWidth(), m.getHeight()))
                        .toList())
                .build();
    }

    private String currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? null : auth.getName();
    }
}
