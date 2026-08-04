package com.fsocial.postservice;

import com.fsocial.postservice.dto.replyComment.LikeReplyCommentDTO;
import com.fsocial.postservice.dto.replyComment.ReplyCommentRequest;
import com.fsocial.postservice.dto.replyComment.ReplyCommentResponse;
import com.fsocial.postservice.dto.replyComment.ReplyCommentUpdateDTORequest;
import com.fsocial.postservice.entity.Comment;
import com.fsocial.postservice.entity.CommentLike;
import com.fsocial.postservice.exception.AppException;
import com.fsocial.postservice.repository.AccountRepository;
import com.fsocial.postservice.repository.CommentLikeRepository;
import com.fsocial.postservice.repository.CommentRepository;
import com.fsocial.postservice.services.AccountService;
import com.fsocial.postservice.services.impl.ReplyCommentServiceImpl;
import com.fsocial.postservice.util.MediaUploadUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Reply giờ là comment có parent_id — test bám model mới, không còn mảng nhúng. */
@ExtendWith(MockitoExtension.class)
class ReplyCommentServiceImplTest {

    @Mock
    private MediaUploadUtils mediaUploadUtils;
    @Mock
    private AccountService accountService;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CommentLikeRepository commentLikeRepository;
    @Mock
    private AccountRepository accountRepository;

    private ReplyCommentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ReplyCommentServiceImpl(
                mediaUploadUtils,
                accountService,
                commentRepository,
                commentLikeRepository,
                accountRepository
        );
    }

    @Test
    @DisplayName("addReplyComment lưu comment con trỏ parent, kế thừa postId")
    void addReplyComment_savesChildComment() {
        Comment parent = comment("comment-1", null);
        parent.setPostId("post-1");
        when(commentRepository.findById("comment-1")).thenReturn(Optional.of(parent));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId("reply-1");
            return c;
        });
        when(accountRepository.findById("user-1")).thenReturn(Optional.empty());

        ReplyCommentRequest request = ReplyCommentRequest.builder()
                .commentId("comment-1").userId("user-1")
                .text("hello").html("<p>hello</p>").build();

        ReplyCommentResponse result = service.addReplyComment(request);

        assertThat(result.getUserId()).isEqualTo("user-1");
        assertThat(result.getCommentId()).isEqualTo("comment-1");
        assertThat(result.getContent().getText()).isEqualTo("hello");

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        assertThat(captor.getValue().getParent()).isSameAs(parent);
        assertThat(captor.getValue().getPostId()).isEqualTo("post-1");
    }

    @Test
    @DisplayName("deleteReplyComment xóa dòng comment, cascade lo like/media")
    void deleteReplyComment_deletesRow() {
        Comment reply = comment("reply-1", comment("comment-1", null));
        when(commentRepository.findById("reply-1")).thenReturn(Optional.of(reply));

        String result = service.deleteReplyComment("reply-1");

        assertThat(result).isEqualTo("Xóa replycomment thành công");
        verify(commentRepository).delete(reply);
    }

    @Test
    @DisplayName("deleteReplyComment từ chối id của comment gốc")
    void deleteReplyComment_rejectsRootComment() {
        when(commentRepository.findById("comment-1")).thenReturn(Optional.of(comment("comment-1", null)));

        assertThatThrownBy(() -> service.deleteReplyComment("comment-1"))
                .isInstanceOf(AppException.class);
        verify(commentRepository, never()).delete(any());
    }

    @Test
    @DisplayName("updateReplyComment ghi text/html phẳng")
    void updateReplyComment_updatesFlatContent() {
        Comment reply = comment("reply-1", comment("comment-1", null));
        reply.setText("old");
        reply.setHtml("old");

        when(accountService.existsById("user-1")).thenReturn(true);
        when(commentRepository.findById("reply-1")).thenReturn(Optional.of(reply));
        when(commentRepository.save(reply)).thenReturn(reply);
        when(accountRepository.findById("user-1")).thenReturn(Optional.empty());

        ReplyCommentUpdateDTORequest request = ReplyCommentUpdateDTORequest.builder()
                .replyCommentId("reply-1").userId("user-1")
                .text("new").html("<p>new</p>").build();

        ReplyCommentResponse updated = service.updateReplyComment(request);

        assertThat(updated.getContent().getText()).isEqualTo("new");
        assertThat(updated.getContent().getHtml()).isEqualTo("<p>new</p>");
        assertThat(updated.getCommentId()).isEqualTo("comment-1");
    }

    @Test
    @DisplayName("updateReplyComment rejects missing user")
    void updateReplyComment_rejectsMissingUser() {
        when(accountService.existsById("user-1")).thenReturn(false);

        ReplyCommentUpdateDTORequest request = ReplyCommentUpdateDTORequest.builder()
                .replyCommentId("reply-1").userId("user-1")
                .text("new").html("<p>new</p>").build();

        assertThatThrownBy(() -> service.updateReplyComment(request))
                .isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("getReplyCommentsByCommentId map like count theo batch")
    void getReplyCommentsByCommentId_mapsReplies() {
        Comment reply = comment("reply-1", comment("comment-1", null));
        reply.setText("hello");
        reply.setHtml("<p>hello</p>");
        reply.setCreateDatetime(LocalDateTime.now());

        when(commentRepository.findByParentId("comment-1")).thenReturn(List.of(reply));
        when(accountRepository.findAllById(any())).thenReturn(List.of());
        when(commentLikeRepository.countByCommentIdIn(List.of("reply-1")))
                .thenReturn(List.<Object[]>of(new Object[]{"reply-1", 2L}));

        List<ReplyCommentResponse> result = service.getReplyCommentsByCommentId("comment-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("reply-1");
        assertThat(result.get(0).getCommentId()).isEqualTo("comment-1");
        assertThat(result.get(0).getCountLikes()).isEqualTo(2);
    }

    @Test
    @DisplayName("likeReplyComment toggle qua comment_like, idempotent theo PK")
    void likeReplyComment_toggles() {
        LikeReplyCommentDTO request = LikeReplyCommentDTO.builder()
                .replyCommentId("reply-1").userId("user-1").build();

        when(commentRepository.existsByIdAndParentIsNotNull("reply-1")).thenReturn(true);
        when(accountService.existsById("user-1")).thenReturn(true);
        when(commentLikeRepository.existsByCommentIdAndUserId("reply-1", "user-1"))
                .thenReturn(false, true);

        assertThat(service.likeReplyComment(request)).isTrue();
        verify(commentLikeRepository).save(any(CommentLike.class));

        assertThat(service.likeReplyComment(request)).isFalse();
        verify(commentLikeRepository).deleteByCommentIdAndUserId("reply-1", "user-1");
    }

    private Comment comment(String id, Comment parent) {
        Comment c = Comment.builder().userId("user-1").parent(parent).build();
        c.setId(id);
        return c;
    }
}
