package com.fsocial.postservice.repository;

import com.fsocial.postservice.entity.SeenPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SeenPostRepository extends JpaRepository<SeenPost, SeenPost.Key> {

    @Query("select s.postId from SeenPost s where s.userId = :userId")
    List<String> findPostIdsByUserId(@Param("userId") String userId);

    boolean existsByUserIdAndPostId(String userId, String postId);

    @Modifying
    @Query("delete from SeenPost s where s.userId = :userId")
    int deleteByUserId(@Param("userId") String userId);

    /** Thay TTL index của Mongo — gọi từ {@code SeenPostPurgeScheduler}. */
    @Modifying
    @Query("delete from SeenPost s where s.seenAt < :threshold")
    int deleteBySeenAtBefore(@Param("threshold") LocalDateTime threshold);
}
