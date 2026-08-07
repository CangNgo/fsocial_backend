package com.fsocial.repository;

import com.fsocial.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, PostLike.Key> {

    int countByPostId(String postId);

    boolean existsByPostIdAndUserId(String postId, String userId);

    @Modifying
    @Query("delete from PostLike l where l.postId = :postId and l.userId = :userId")
    int deleteByPostIdAndUserId(@Param("postId") String postId, @Param("userId") String userId);

    @Query("select l.userId from PostLike l where l.postId = :postId")
    List<String> findUserIdsByPostId(@Param("postId") String postId);

    /** Đếm like cho nhiều post trong 1 truy vấn: [postId, count]. */
    @Query("select l.postId, count(l) from PostLike l where l.postId in :postIds group by l.postId")
    List<Object[]> countByPostIdIn(@Param("postIds") List<String> postIds);

    /** Cờ isLike cho cả trang feed trong 1 truy vấn thay vì N lần existsBy. */
    @Query("select l.postId from PostLike l where l.userId = :userId and l.postId in :postIds")
    List<String> findLikedPostIds(@Param("userId") String userId, @Param("postIds") List<String> postIds);

    @Modifying
    @Query("delete from PostLike l where l.postId = :postId")
    int deleteByPostId(@Param("postId") String postId);
}
