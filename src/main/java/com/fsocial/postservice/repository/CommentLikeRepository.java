package com.fsocial.postservice.repository;

import com.fsocial.postservice.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLike.Key> {

    int countByCommentId(String commentId);

    boolean existsByCommentIdAndUserId(String commentId, String userId);

    @Modifying
    @Query("delete from CommentLike l where l.commentId = :commentId and l.userId = :userId")
    int deleteByCommentIdAndUserId(@Param("commentId") String commentId, @Param("userId") String userId);

    @Query("select l.userId from CommentLike l where l.commentId = :commentId")
    List<String> findUserIdsByCommentId(@Param("commentId") String commentId);

    /** Đếm like cho nhiều comment trong 1 truy vấn: [commentId, count]. */
    @Query("select l.commentId, count(l) from CommentLike l where l.commentId in :commentIds group by l.commentId")
    List<Object[]> countByCommentIdIn(@Param("commentIds") List<String> commentIds);

    /** Cờ isLike cho cả danh sách comment trong 1 truy vấn thay vì N lần existsBy. */
    @Query("select l.commentId from CommentLike l where l.userId = :userId and l.commentId in :commentIds")
    List<String> findLikedCommentIds(@Param("userId") String userId,
                                     @Param("commentIds") List<String> commentIds);
}
