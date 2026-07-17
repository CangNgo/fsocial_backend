package com.fsocial.postservice.services.impl;

import com.fsocial.postservice.dto.replyComment.LikeReplyCommentDTO;
import com.fsocial.postservice.dto.replyComment.ReplyCommentRequest;
import com.fsocial.postservice.dto.replyComment.ReplyCommentResponse;
import com.fsocial.postservice.dto.replyComment.ReplyCommentUpdateDTORequest;
import com.fsocial.postservice.entity.Account;
import com.fsocial.postservice.entity.Comment;
import com.fsocial.postservice.entity.Content;
import com.fsocial.postservice.entity.MediaItem;
import com.fsocial.postservice.entity.ReplyComment;
import com.fsocial.postservice.exception.AppException;
import com.fsocial.postservice.exception.StatusCode;
import com.fsocial.postservice.repository.AccountRepository;
import com.fsocial.postservice.repository.CommentRepository;
import com.fsocial.postservice.services.AccountService;
import com.fsocial.postservice.services.ReplyCommentService;
import com.fsocial.postservice.util.DisplayNameUtils;
import com.fsocial.postservice.util.MediaUploadUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ReplyCommentServiceImpl implements ReplyCommentService {

    MediaUploadUtils mediaUploadUtils;
    AccountService accountService;
    MongoTemplate mongoTemplate;
    CommentRepository commentRepository;
    AccountRepository accountRepository;

    @Override
    public ReplyComment addReplyComment(ReplyCommentRequest request) throws IOException {
        Comment comment = commentRepository.findById(request.getCommentId()).orElseThrow(
                () -> new AppException("Không tìm thấy comment ", StatusCode.COMMENT_NOT_FOUND));

        MediaItem[] mediaItems = request.getMedia() == null || request.getMedia().length == 0
                ? new MediaItem[0]
                : mediaUploadUtils.uploadValidMedia(request.getMedia());

        ReplyComment replyComment = new ReplyComment();
        replyComment.setUserId(request.getUserId());
        replyComment.setContent(Content.builder()
                .media(mediaItems.length > 0 ? Arrays.asList(mediaItems) : null)
                .text(request.getText())
                .html(request.getHtml())
                .build());
        replyComment.setLikes(new ArrayList<>());
        replyComment.setCreateDatetime(LocalDateTime.now());
        replyComment.setCreatedAt(LocalDateTime.now());

        comment.setReply(true);
        List<ReplyComment> replies = comment.getReplies() == null ? new ArrayList<>() : new ArrayList<>(comment.getReplies());
        replies.add(replyComment);
        comment.setReplies(replies);
        commentRepository.save(comment);
        return replyComment;
    }

    @Override
    public String deleteReplyComment(String idReplyComment) {
        Comment comment = findCommentByReplyId(idReplyComment);
        List<ReplyComment> replies = comment.getReplies() == null ? new ArrayList<>() : new ArrayList<>(comment.getReplies());
        boolean removed = replies.removeIf(reply -> idReplyComment.equals(reply.getId()));
        if (!removed) {
            throw new AppException("Reply comment không tồn tại", StatusCode.REPLY_COMMENT_NOT_FOUND);
        }
        comment.setReplies(replies);
        comment.setReply(!replies.isEmpty());
        commentRepository.save(comment);
        return "Xóa replycomment thành công";
    }

    @Override
    public List<ReplyCommentResponse> getReplyCommentsByCommentId(String commentId) {
        return commentRepository.findById(commentId)
                .map(comment -> toReplyCommentResponses(commentId, comment.getReplies()))
                .orElse(List.of());
    }

    @Override
    public ReplyComment updateReplyComment(ReplyCommentUpdateDTORequest updateReply) {
        if (!userExists(updateReply.getUserId())) {
            throw new AppException("User không tồn tại", StatusCode.USER_NOT_FOUND);
        }

        Comment comment = findCommentByReplyId(updateReply.getReplyCommentId());
        ReplyComment replyComment = findReply(comment, updateReply.getReplyCommentId());
        replyComment.setContent(Content.builder()
                .text(updateReply.getText())
                .html(updateReply.getHtml())
                .build());
        commentRepository.save(comment);
        return replyComment;
    }

    @Override
    public boolean likeReplyComment(LikeReplyCommentDTO request) {
        if (!replyCommentExists(request.getReplyCommentId())) {
            throw new AppException("Reply comment not found", StatusCode.REPLY_COMMENT_NOT_FOUND);
        }
        if (!userExists(request.getUserId())) {
            throw new AppException("User not found", StatusCode.USER_NOT_FOUND);
        }

        boolean exists = replyCommentLikedByUser(request.getReplyCommentId(), request.getUserId());
        if (!exists) {
            addLike(request.getReplyCommentId(), request.getUserId());
            return true;
        }

        removeLike(request.getReplyCommentId(), request.getUserId());
        return false;
    }

    public void addLike(String replyCommentId, String userId) {
        Query query = new Query(Criteria.where("replies._id").is(replyCommentId));
        Update update = new Update().addToSet("replies.$.likes", userId);
        mongoTemplate.updateFirst(query, update, Comment.class);
    }

    public void removeLike(String replyCommentId, String userId) {
        Query query = new Query(Criteria.where("replies._id").is(replyCommentId));
        Update update = new Update().pull("replies.$.likes", userId);
        mongoTemplate.updateFirst(query, update, Comment.class);
    }

    private boolean userExists(String userId) {
        return accountService.existsById(userId);
    }

    private boolean replyCommentExists(String replyCommentId) {
        return commentRepository.findByRepliesId(replyCommentId).isPresent();
    }

    private boolean replyCommentLikedByUser(String replyCommentId, String userId) {
        Query query = new Query(Criteria.where("replies")
                .elemMatch(Criteria.where("_id").is(replyCommentId).and("likes").is(userId)));
        return mongoTemplate.exists(query, Comment.class);
    }

    private Comment findCommentByReplyId(String replyCommentId) {
        return commentRepository.findByRepliesId(replyCommentId).orElseThrow(
                () -> new AppException("Reply comment không tồn tại", StatusCode.REPLY_COMMENT_NOT_FOUND));
    }

    private ReplyComment findReply(Comment comment, String replyCommentId) {
        List<ReplyComment> replies = comment.getReplies() == null ? List.of() : comment.getReplies();
        return replies.stream()
                .filter(reply -> replyCommentId.equals(reply.getId()))
                .findFirst()
                .orElseThrow(() -> new AppException("Reply comment không tồn tại", StatusCode.REPLY_COMMENT_NOT_FOUND));
    }

    private List<ReplyCommentResponse> toReplyCommentResponses(String commentId, List<ReplyComment> replies) {
        if (replies == null || replies.isEmpty()) {
            return List.of();
        }

        List<String> ownerIds = replies.stream().map(ReplyComment::getUserId).distinct().toList();
        Map<String, Account> accountMap = accountRepository.findAllById(ownerIds).stream()
                .collect(Collectors.toMap(Account::getId, Function.identity()));

        return replies.stream()
                .map(reply -> toReplyCommentResponse(commentId, reply, accountMap.get(reply.getUserId())))
                .toList();
    }

    private ReplyCommentResponse toReplyCommentResponse(String commentId, ReplyComment reply, Account owner) {
        List<String> likes = reply.getLikes() == null ? List.of() : reply.getLikes();
        return ReplyCommentResponse.builder()
                .id(reply.getId())
                .commentId(commentId)
                .userId(reply.getUserId())
                .content(reply.getContent())
                .countLikes(likes.size())
                .displayName(DisplayNameUtils.build(owner))
                .avatar(owner == null ? null : owner.getAvatar())
                .createDatetime(reply.getCreateDatetime())
                .build();
    }
}
