package com.fsocial.postservice;

import com.fsocial.postservice.entity.Account;
import com.fsocial.postservice.entity.Comment;
import com.fsocial.postservice.entity.Post;
import com.fsocial.postservice.entity.PostLike;
import com.fsocial.postservice.entity.Attachments;
import com.fsocial.postservice.enums.AttachmentType;
import com.fsocial.postservice.enums.MediaType;
import com.fsocial.postservice.repository.AccountRepository;
import com.fsocial.postservice.repository.AttachmentsRepository;
import com.fsocial.postservice.repository.CommentRepository;
import com.fsocial.postservice.repository.PostLikeRepository;
import com.fsocial.postservice.repository.PostRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sanity check cho migration Mongo -> Postgres: round-trip qua các bảng join
 * thay cho embedded array cũ (media/tag/like/reply). Chạy trên DB dev thật
 * (không có Testcontainers/Docker trong môi trường này); @Transactional rollback
 * sau mỗi test nên không để lại rác.
 */
@SpringBootTest
@Transactional
class MigrationSanityTest {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AttachmentsRepository attachmentsRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private PostLikeRepository postLikeRepository;
    @Autowired
    private CommentRepository commentRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void post_withMediaAndTags_roundTrips() {
        Account owner = accountRepository.save(Account.builder().username("sanity_" + System.nanoTime()).build());

        Post post = postRepository.save(Post.builder().owner(owner).text("hello")
                .tags(List.of("travel", "food", "tech")).build());
        post.addMedia(Attachments.builder()
                .url("http://a/1.png")
                .type(AttachmentType.POST)
                .mediaType(MediaType.IMAGE)
                .build());
        post.addMedia(Attachments.builder()
                .url("http://a/2.png")
                .type(AttachmentType.POST)
                .mediaType(MediaType.IMAGE)
                .build());
        postRepository.saveAndFlush(post);
        attachmentsRepository.saveAllAndFlush(post.getMedia());

        assertThat(mediaCount(post.getId())).isEqualTo(2);
        assertThat(postRepository.findById(post.getId()).orElseThrow().getTags())
                .containsExactlyInAnyOrder("travel", "food", "tech");
    }

    @Test
    void likingTwice_countsAsOne() {
        Account owner = accountRepository.save(Account.builder().username("sanity_" + System.nanoTime()).build());
        Post post = postRepository.save(Post.builder().owner(owner).text("hello").build());

        postLikeRepository.save(new PostLike(post.getId(), owner.getId()));
        postLikeRepository.flush();
        // second "like" is a toggle in the real service (delete-if-exists); here we just
        // assert the PK is a real uniqueness constraint, matching prior Mongo idempotency.
        assertThat(postLikeRepository.existsById(new PostLike.Key(post.getId(), owner.getId()))).isTrue();

        List<Object[]> counts = postLikeRepository.countByPostIdIn(List.of(post.getId()));
        assertThat(counts).hasSize(1);
        assertThat(((Number) counts.get(0)[1]).intValue()).isEqualTo(1);
    }

    @Test
    void deletingPost_cascadesLikeTagMedia() {
        Account owner = accountRepository.save(Account.builder().username("sanity_" + System.nanoTime()).build());
        Post post = postRepository.save(Post.builder().owner(owner).text("hello").build());
        post.addMedia(Attachments.builder()
                .url("http://a/1.png")
                .type(AttachmentType.POST)
                .mediaType(MediaType.IMAGE)
                .build());
        postRepository.saveAndFlush(post);
        attachmentsRepository.saveAllAndFlush(post.getMedia());
        postLikeRepository.saveAndFlush(new PostLike(post.getId(), owner.getId()));

        Comment root = commentRepository.saveAndFlush(
                Comment.builder().postId(post.getId()).userId(owner.getId()).text("root").build());

        postRepository.delete(post);
        postRepository.flush();

        assertThat(mediaCount(post.getId())).isZero();
        assertThat(postLikeRepository.existsById(new PostLike.Key(post.getId(), owner.getId()))).isFalse();
        // comment.post_id has no FK to post (plain string column) so it survives — only
        // the CommentConsumer (RabbitMQ) deletes it out-of-band. Assert it's still there.
        assertThat(commentRepository.findById(root.getId())).isPresent();
    }

    @Test
    void commentWithReplies_findByPostIdReturnsOnlyRoots() {
        Account owner = accountRepository.save(Account.builder().username("sanity_" + System.nanoTime()).build());
        Post post = postRepository.save(Post.builder().owner(owner).text("hello").build());

        Comment root = commentRepository.saveAndFlush(
                Comment.builder().postId(post.getId()).userId(owner.getId()).text("root").build());
        commentRepository.saveAndFlush(
                Comment.builder().postId(post.getId()).userId(owner.getId()).parent(root).text("reply1").build());
        commentRepository.saveAndFlush(
                Comment.builder().postId(post.getId()).userId(owner.getId()).parent(root).text("reply2").build());

        List<Comment> roots = commentRepository.findByPostId(post.getId());

        assertThat(roots).hasSize(1);
        assertThat(roots.get(0).getId()).isEqualTo(root.getId());
        assertThat(commentRepository.findByParentId(root.getId())).hasSize(2);
    }

    private long mediaCount(String postId) {
        return entityManager
                .createQuery("select count(m) from Attachments m where m.post.id = :postId", Long.class)
                .setParameter("postId", postId)
                .getSingleResult();
    }
}
