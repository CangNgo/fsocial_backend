package com.fsocial.postservice.services.impl;

import com.fsocial.postservice.dto.comment.CommentDTO;
import com.fsocial.postservice.dto.comment.CommentDTORequest;
import com.fsocial.postservice.dto.comment.CommentResponse;
import com.fsocial.postservice.dto.comment.CommentUpdateDTORequest;
import com.fsocial.postservice.entity.Account;
import com.fsocial.postservice.entity.Comment;
import com.fsocial.postservice.entity.Content;
import com.fsocial.postservice.entity.Post;
import com.fsocial.postservice.exception.AppCheckedException;
import com.fsocial.postservice.exception.StatusCode;
import com.fsocial.postservice.publisher.InteractionEventPublisher;
import com.fsocial.postservice.repository.AccountRepository;
import com.fsocial.postservice.repository.CommentRepository;
import com.fsocial.postservice.repository.PostRepository;
import com.fsocial.postservice.services.AccountService;
import com.fsocial.postservice.services.CommentService;
import com.fsocial.postservice.services.RedisService;
import com.fsocial.postservice.util.DisplayNameUtils;
import com.fsocial.postservice.util.MediaUploadUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CommentServiceImpl implements CommentService {
    CommentRepository commentRepository;
    MediaUploadUtils mediaUploadUtils;
    PostRepository postRepository;
    MongoTemplate mongoTemplate;
    AccountService accountService;
    AccountRepository accountRepository;
    RedisService redisService;
    InteractionEventPublisher interactionEventPublisher;

    @Override
    @Transactional
    public Comment addComment(CommentDTORequest request) throws AppCheckedException {
        String[] mediaUrls = mediaUploadUtils.uploadValidMedia(request.getMedia());

        String postId = request.getPostId();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppCheckedException("Không tìm thấy bài đăng", StatusCode.POST_NOT_FOUND));

        Comment commentRequest = buildComment(request, mediaUrls);
        commentRequest.setCreatedAt(LocalDateTime.now());
        commentRequest.setLikes(new ArrayList<>());
        commentRequest.setCreateDatetime(LocalDateTime.now());
        Comment savedComment = commentRepository.save(commentRequest);

        redisService.personalization(savedComment.getUserId(), post.getOwner().getUserId());

        // Publish async COMMENT event for score + interest update
        interactionEventPublisher.publish(postId, request.getUserId(), "COMMENT", post.getTags());

        return savedComment;
    }

    private Comment buildComment(CommentDTORequest request, String[] mediaUrls) {
        return Comment.builder()
                .likes(new ArrayList<>())
                .reply(false)
                .postId(request.getPostId())
                .userId(request.getUserId())
                .content(Content.builder()
                        .text(request.getText())
                        .media(mediaUrls)
                        .html(request.getHtml())
                        .build())
                .build();
    }

    @Override
    public boolean toggleLikeComment(String commentId, String userId) throws AppCheckedException {
        boolean existed = commentRepository.existsByIdAndLikes(commentId, userId);
        if (!existed) {
            this.addLikeComment(commentId, userId);
            return true;
        } else {
            this.removeLikeComment(commentId, userId);
            return false;
        }
    }

    @Override
    public Integer countLike(String commentId, String userId) {
        Integer count = commentRepository.countLikes(commentId);
        return count == null ? 0 : count;
    }

    @Override
    public Comment updateComment(CommentUpdateDTORequest comment) throws AppCheckedException {
        if (userExists(comment.getUserId()))
            throw new AppCheckedException("User không tồn tại", StatusCode.USER_NOT_FOUND);

        Comment update = commentRepository.findById(comment.getCommentId()).orElseThrow(() -> new AppCheckedException("Không tìm thấy comment", StatusCode.COMMENT_NOT_FOUND));
        //cập nhật text
        update.setContent(Content.builder()
                        .html(comment.getHtml())
                        .text(comment.getText())
                .build());
        return commentRepository.save(update);
    }

    @Override
    public String deleteComment(String commentID) {
        commentRepository.deleteById(commentID);
        return "Xóa comment thành công";
    }

    public void addLikeComment(String commentId, String userId) throws AppCheckedException {
        boolean check = this.userExists(userId);
        if (!this.commentExist(commentId))
            throw new AppCheckedException("Bình luân không tồn tại", StatusCode.COMMENT_NOT_FOUND);
        if (!this.userExists(userId))
            throw new AppCheckedException("Tài khoản người dùng không tồn tại", StatusCode.USER_NOT_FOUND);

        Query query = new Query(Criteria.where("_id").is(commentId));
        Update update = new Update().addToSet("likes", userId);
        mongoTemplate.updateFirst(query, update, Comment.class);

    }

    public void removeLikeComment(String commentId, String userId) throws AppCheckedException {
        if (!this.commentExist(commentId))
            throw new AppCheckedException("Bình luân không tồn tại", StatusCode.COMMENT_NOT_FOUND);
        if (!this.userExists(userId))
            throw new AppCheckedException("Tài khoản người dùng không tồn tại", StatusCode.USER_NOT_FOUND);

        Query query = new Query(Criteria.where("_id").is(commentId));
        Update update = new Update().pull("likes", userId);
        mongoTemplate.updateFirst(query, update, Comment.class);
    }

    public boolean userExists(String userId) {
        return accountService.existsById(userId);
    }

    public boolean commentExist(String commentId) {
        return commentRepository.existsById(commentId);
    }

    @Override
    public List<CommentResponse> getComments(String postId) {
        List<Comment> comments = commentRepository.findByPostId(postId);
        return toCommentResponses(comments, currentUserId());
    }

    @Override
    public CommentResponse convertToCommentResponse(Comment comment) {
        Account owner = accountRepository.findById(comment.getUserId()).orElse(null);
        return buildCommentResponse(comment, owner, currentUserId());
    }

    @Override
    public List<CommentDTO> deleteCommentByPostId(String postId) throws AppCheckedException {
        commentRepository.deleteAll(commentRepository.findByPostId(postId));
        return List.of();
    }

    private List<CommentResponse> toCommentResponses(List<Comment> comments, String requesterId) {
        if (comments.isEmpty()) return List.of();

        List<String> ownerIds = comments.stream().map(Comment::getUserId).distinct().toList();
        Map<String, Account> accountMap = accountRepository.findAllById(ownerIds).stream()
                .collect(Collectors.toMap(Account::getId, Function.identity()));

        return comments.stream()
                .map(c -> buildCommentResponse(c, accountMap.get(c.getUserId()), requesterId))
                .collect(Collectors.toList());
    }

    private CommentResponse buildCommentResponse(Comment comment, Account owner, String requesterId) {
        List<String> likes = comment.getLikes() == null ? List.of() : comment.getLikes();
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .countLikes(likes.size())
                .displayName(DisplayNameUtils.build(owner))
                .userId(comment.getUserId())
                .reply(Boolean.TRUE.equals(comment.getReply()))
                .like(requesterId != null && likes.contains(requesterId))
                .createDatetime(comment.getCreateDatetime())
                .build();
    }

    private String currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? null : auth.getName();
    }
}
