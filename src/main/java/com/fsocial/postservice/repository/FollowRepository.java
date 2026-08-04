package com.fsocial.postservice.repository;

import com.fsocial.postservice.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Follow.Key> {

    boolean existsByFollowerIdAndFolloweeId(String followerId, String followeeId);

    @Modifying
    @Query("delete from Follow f where f.followerId = :followerId and f.followeeId = :followeeId")
    int deleteByFollowerIdAndFolloweeId(@Param("followerId") String followerId,
                                        @Param("followeeId") String followeeId);

    /** Ai đang follow user này. */
    @Query("select f.followerId from Follow f where f.followeeId = :userId")
    List<String> findFollowerIds(@Param("userId") String userId);

    /** User này đang follow ai. */
    @Query("select f.followeeId from Follow f where f.followerId = :userId")
    List<String> findFolloweeIds(@Param("userId") String userId);

    long countByFolloweeId(String followeeId);

    long countByFollowerId(String followerId);
}
