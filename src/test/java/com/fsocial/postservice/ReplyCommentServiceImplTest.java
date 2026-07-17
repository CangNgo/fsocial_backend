package com.fsocial.postservice;

import com.fsocial.postservice.dto.replyComment.ReplyCommentRequest;
import com.fsocial.postservice.dto.replyComment.ReplyCommentResponse;
import com.fsocial.postservice.dto.replyComment.ReplyCommentUpdateDTORequest;
import com.fsocial.postservice.entity.Comment;
import com.fsocial.postservice.entity.Content;
import com.fsocial.postservice.entity.ReplyComment;
import com.fsocial.postservice.exception.AppException;
import com.fsocial.postservice.repository.AccountRepository;
import com.fsocial.postservice.repository.CommentRepository;
import com.fsocial.postservice.services.AccountService;
import com.fsocial.postservice.services.impl.ReplyCommentServiceImpl;
import com.fsocial.postservice.util.MediaUploadUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReplyCommentServiceImplTest {

    @Mock
    private MediaUploadUtils mediaUploadUtils;
    @Mock
    private AccountService accountService;
    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private AccountRepository accountRepository;

    private ReplyCommentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ReplyCommentServiceImpl(
                mediaUploadUtils,
                accountService,
                mongoTemplate,
                commentRepository,
                accountRepository
        );
    }

    @Test
    @DisplayName("addReplyComment embeds reply and marks comment as replied")
    void addReplyComment_embedsReply() throws Exception {
        Comment comment = Comment.builder()
                .postId("post-1")
                .userId("owner")
                .reply(false)
                .replies(new ArrayList<>())
                .build();
        comment.setId("comment-1");

        ReplyCommentRequest request = ReplyCommentRequest.builder()
                .commentId("comment-1")
                .userId("user-1")
                .text("hello")
                .html("<p>hello</p>")
                .build();

        ReplyComment result = service.addReplyComment(request);

        assertThat(result.getUserId()).isEqualTo("user-1");
        assertThat(comment.getReply()).isTrue();
        assertThat(comment.getReplies()).hasSize(1);
        assertThat(comment.getReplies().get(0).getContent().getText()).isEqualTo("hello");
        verify(commentRepository).findById("comment-1");
        verify(commentRepository).save(comment);
    }

    @Test
    @DisplayName("deleteReplyComment removes last reply and resets flag")
    void deleteReplyComment_resetsReplyFlag() throws Exception {
        ReplyComment reply = new ReplyComment();
        reply.setId("reply-1");
        reply.setUserId("user-1");

        Comment comment = Comment.builder()
                .reply(true)
                .replies(new ArrayList<>(List.of(reply)))
                .build();
        comment.setId("comment-1");

        when(commentRepository.findByRepliesId("reply-1")).thenReturn(Optional.of(comment));

        String result = service.deleteReplyComment("reply-1");

        assertThat(result).isEqualTo("Xóa replycomment thành công");
        assertThat(comment.getReply()).isFalse();
        assertThat(comment.getReplies()).isEmpty();
        verify(commentRepository).save(comment);
    }

    @Test
    @DisplayName("updateReplyComment changes nested content")
    void updateReplyComment_updatesNestedContent() throws Exception {
        ReplyComment reply = new ReplyComment();
        reply.setId("reply-1");
        reply.setUserId("user-1");
        reply.setContent(Content.builder().text("old").html("old").build());

        Comment comment = Comment.builder()
                .reply(true)
                .replies(new ArrayList<>(List.of(reply)))
                .build();

        ReplyCommentUpdateDTORequest request = ReplyCommentUpdateDTORequest.builder()
                .replyCommentId("reply-1")
                .userId("user-1")
                .text("new")
                .html("<p>new</p>")
                .build();

        when(accountService.existsById("user-1")).thenReturn(true);
        when(commentRepository.findByRepliesId("reply-1")).thenReturn(Optional.of(comment));

        ReplyComment updated = service.updateReplyComment(request);

        assertThat(updated.getContent().getText()).isEqualTo("new");
        assertThat(updated.getContent().getHtml()).isEqualTo("<p>new</p>");
        verify(commentRepository).save(comment);
    }

    @Test
    @DisplayName("getReplyCommentsByCommentId maps embedded replies")
    void getReplyCommentsByCommentId_mapsReplies() {
        ReplyComment reply = new ReplyComment();
        reply.setId("reply-1");
        reply.setUserId("user-1");
        reply.setLikes(new ArrayList<>(List.of("u2", "u3")));
        reply.setContent(Content.builder().text("hello").html("<p>hello</p>").build());
        reply.setCreateDatetime(LocalDateTime.now());

        Comment comment = Comment.builder()
                .reply(true)
                .replies(List.of(reply))
                .build();

        when(commentRepository.findById("comment-1")).thenReturn(Optional.of(comment));
        when(accountRepository.findAllById(any())).thenReturn(List.of());

        List<ReplyCommentResponse> result = service.getReplyCommentsByCommentId("comment-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("reply-1");
        assertThat(result.get(0).getCommentId()).isEqualTo("comment-1");
        assertThat(result.get(0).getCountLikes()).isEqualTo(2);
    }

    @Test
    @DisplayName("updateReplyComment rejects missing user")
    void updateReplyComment_rejectsMissingUser() {
        ReplyCommentUpdateDTORequest request = ReplyCommentUpdateDTORequest.builder()
                .replyCommentId("reply-1")
                .userId("user-1")
                .text("new")
                .html("<p>new</p>")
                .build();

        when(accountService.existsById("user-1")).thenReturn(false);

        assertThatThrownBy(() -> service.updateReplyComment(request))
                .isInstanceOf(AppException.class);
    }
}
