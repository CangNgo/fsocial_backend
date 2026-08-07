package com.fsocial.repository;

import com.fsocial.entity.UserInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserInterestRepository extends JpaRepository<UserInterest, UserInterest.Key> {

    List<UserInterest> findByUserId(String userId);

    List<UserInterest> findByUserIdOrderByWeightDesc(String userId);

    boolean existsByUserId(String userId);

    @Modifying
    @Query("delete from UserInterest i where i.userId = :userId")
    int deleteByUserId(@Param("userId") String userId);

    /** Cộng dồn weight — thay upsert + positional-inc của Mongo. */
    @Modifying
    @Query(value = """
            insert into user_interest(user_id, tag, weight, updated_at)
            values (:userId, :tag, :delta, now())
            on conflict (user_id, tag)
            do update set weight = user_interest.weight + :delta, updated_at = now()
            """, nativeQuery = true)
    int incrementWeight(@Param("userId") String userId,
                        @Param("tag") String tag,
                        @Param("delta") double delta);

    @Modifying
    @Query("update UserInterest i set i.weight = i.weight * :factor, i.updatedAt = current_timestamp")
    int decayAll(@Param("factor") double factor);

    @Modifying
    @Query("delete from UserInterest i where i.weight < :threshold")
    int deleteBelowWeight(@Param("threshold") double threshold);
}
