package com.fsocial.postservice.services.impl;

import com.fsocial.postservice.dto.post.MediaItemDTO;
import com.fsocial.postservice.dto.replyComment.LikeReplyCommentDTO;
import com.fsocial.postservice.dto.replyComment.ReplyCommentRequest;
import com.fsocial.postservice.dto.replyComment.ReplyCommentResponse;
import com.fsocial.postservice.dto.replyComment.ReplyCommentUpdateDTORequest;
import com.fsocial.postservice.entity.Account;
import com.fsocial.postservice.entity.Comment;
import com.fsocial.postservice.entity.CommentLike;
import com.fsocial.postservice.entity.CommentMedia;
import com.fsocial.postservice.exception.AppException;
import com.fsocial.postservice.exception.StatusCode;
import com.fsocial.postservice.repository.AccountRepository;
import com.fsocial.postservice.repository.CommentLikeRepository;
import com.fsocial.postservice.repository.CommentRepository;
import com.fsocial.postservice.services.AccountService;
import com.fsocial.postservice.services.ReplyCommentService;
import com.fsocial.postservice.util.DisplayNameUtils;
import com.fsocial.postservice.util.MediaUploadUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Reply giờ chỉ là một dòng {@code comment} có {@code parent_id} — không còn mảng nhúng
 * nên mọi thao tác là CRUD thường, like đi qua {@code comment_like} như comment gốc.
 */
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ReplyCommentServiceImpl implements ReplyCommentService {

    MediaUploadUtils mediaUploadUtils;
    AccountService accountService;
    CommentRepository commentRepository;
    CommentLikeRepository commentLikeRepository;
    AccountRepository accountRepository;

    @Override
    @Transactional
    public ReplyCommentResponse addReplyComment(ReplyCommentRequest request) {
        Comment parent = commentRepository.findById(request.getCommentId()).orElseThrow(
                () -> new AppException("Không tìm thấy comment ", StatusCode.COMMENT_NOT_FOUND));

        MediaItemDTO[] mediaItems = mediaUploadUtils.uploadValidMedia(request.getMedia());

        Comment reply = Comment.builder()
                .postId(parent.getPostId())
                .parent(parent)
                .userId(request.getUserId())
                .text(request.getText())
                .html(request.getHtml())
                .createDatetime(LocalDateTime.now())
                .build();
        attachMedia(reply, mediaItems);
        Comment saved = commentRepository.save(reply);

        Account owner = accountRepository.findById(saved.getUserId()).orElse(null);
        return toReplyCommentResponse(parent.getId(), saved, owner, 0, false);
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
    public String deleteReplyComment(String idReplyComment) {
        Comment reply = findReply(idReplyComment);
        // comment_like / comment_media cascade theo FK
        commentRepository.delete(reply);
        return "Xóa replycomment thành công";
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReplyCommentResponse> getReplyCommentsByCommentId(String commentId) {
        List<Comment> replies = commentRepository.findByParentId(commentId);
        if (replies.isEmpty()) return List.of();

        List<String> replyIds = replies.stream().map(Comment::getId).toList();
        List<String> ownerIds = replies.stream().map(Comment::getUserId).distinct().toList();

        Map<String, Account> accountMap = accountRepository.findAllById(ownerIds).stream()
                .collect(Collectors.toMap(Account::getId, Function.identity()));

        Map<String, Integer> likeCountMap = commentLikeRepository.countByCommentIdIn(replyIds).stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> ((Number) r[1]).intValue()));

        String requesterId = currentUserId();
        Set<String> liked = requesterId == null
                ? Set.of()
                : new HashSet<>(commentLikeRepository.findLikedCommentIds(requesterId, replyIds));

        return replies.stream()
                .map(r -> toReplyCommentResponse(commentId, r, accountMap.get(r.getUserId()),
                        likeCountMap.getOrDefault(r.getId(), 0), liked.contains(r.getId())))
                .toList();
    }

    @Override
    @Transactional
    public ReplyCommentResponse updateReplyComment(ReplyCommentUpdateDTORequest updateReply) {
        if (!userExists(updateReply.getUserId())) {
            throw new AppException("User không tồn tại", StatusCode.USER_NOT_FOUND);
        }

        Comment reply = findReply(updateReply.getReplyCommentId());
        reply.setText(updateReply.getText());
        reply.setHtml(updateReply.getHtml());
        Comment saved = commentRepository.save(reply);

        Account owner = accountRepository.findById(saved.getUserId()).orElse(null);
        String requesterId = currentUserId();
        return toReplyCommentResponse(saved.getParent().getId(), saved, owner,
                commentLikeRepository.countByCommentId(saved.getId()),
                requesterId != null
                        && commentLikeRepository.existsByCommentIdAndUserId(saved.getId(), requesterId));
    }

    @Override
    @Transactional
    public boolean likeReplyComment(LikeReplyCommentDTO request) {
        String replyId = request.getReplyCommentId();
        if (!replyCommentExists(replyId)) {
            throw new AppException("Reply comment not found", StatusCode.REPLY_COMMENT_NOT_FOUND);
        }
        if (!userExists(request.getUserId())) {
            throw new AppException("User not found", StatusCode.USER_NOT_FOUND);
        }

        if (!commentLikeRepository.existsByCommentIdAndUserId(replyId, request.getUserId())) {
            // PK (comment_id, user_id) đảm bảo idempotent
            commentLikeRepository.save(new CommentLike(replyId, request.getUserId()));
            return true;
        }
        commentLikeRepository.deleteByCommentIdAndUserId(replyId, request.getUserId());
        return false;
    }

    private boolean userExists(String userId) {
        return accountService.existsById(userId);
    }

    private boolean replyCommentExists(String replyCommentId) {
        return commentRepository.existsByIdAndParentIsNotNull(replyCommentId);
    }

    /** Chỉ chấp nhận comment có parent — comment gốc không phải reply. */
    private Comment findReply(String replyCommentId) {
        Comment comment = commentRepository.findById(replyCommentId).orElseThrow(
                () -> new AppException("Reply comment không tồn tại", StatusCode.REPLY_COMMENT_NOT_FOUND));
        if (comment.getParent() == null) {
            throw new AppException("Reply comment không tồn tại", StatusCode.REPLY_COMMENT_NOT_FOUND);
        }
        return comment;
    }

    private ReplyCommentResponse toReplyCommentResponse(String commentId, Comment reply, Account owner,
                                                        int likeCount, boolean liked) {
        return ReplyCommentResponse.builder()
                .id(reply.getId())
                .commentId(commentId)
                .userId(reply.getUserId())
                .content(CommentServiceImpl.buildContent(reply))
                .countLikes(likeCount)
                .displayName(DisplayNameUtils.build(owner))
                .avatar(owner == null ? null : owner.getAvatar())
                .createDatetime(reply.getCreateDatetime())
                .like(liked)
                .build();
    }

    private String currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? null : auth.getName();
    }
}
