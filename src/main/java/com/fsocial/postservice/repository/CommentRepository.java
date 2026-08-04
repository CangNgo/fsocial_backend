package com.fsocial.postservice.repository;

import com.fsocial.postservice.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {

    /** Chỉ comment gốc (parent_id IS NULL) — reply lấy qua {@link #findByParentId}. */
    @Query("select c from Comment c where c.postId = :postId and c.parent is null order by c.createDatetime asc")
    List<Comment> findByPostId(@Param("postId") String postId);

    @Query("select count(c) from Comment c where c.postId = :postId and c.parent is null")
    Integer countByPostId(@Param("postId") String postId);

    @Query("select c from Comment c where c.parent.id = :parentId order by c.createDatetime asc")
    List<Comment> findByParentId(@Param("parentId") String parentId);

    @Query("""
            select c.postId, count(c) from Comment c
            where c.postId in :postIds and c.parent is null
            group by c.postId
            """)
    List<Object[]> countByPostIdInRaw(@Param("postIds") List<String> postIds);

    default List<PostCommentCount> countByPostIdIn(List<String> postIds) {
        return countByPostIdInRaw(postIds).stream()
                .map(r -> new PostCommentCount((String) r[0], ((Number) r[1]).intValue()))
                .toList();
    }

    @Query("select c.postId from Comment c where c.id = :commentId")
    Optional<String> findPostIdById(@Param("commentId") String commentId);

    /** Số reply của từng comment gốc trong 1 truy vấn: [parentId, count]. */
    @Query("select c.parent.id, count(c) from Comment c where c.parent.id in :parentIds group by c.parent.id")
    List<Object[]> countByParentIdIn(@Param("parentIds") List<String> parentIds);

    /** True chỉ khi id tồn tại VÀ là reply (có parent). */
    boolean existsByIdAndParentIsNotNull(String id);

    void deleteByPostId(String postId);

    record PostCommentCount(String _id, Integer count) {}
}
