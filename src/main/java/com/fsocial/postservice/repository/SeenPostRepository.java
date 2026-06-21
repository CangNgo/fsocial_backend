package com.fsocial.postservice.repository;

import com.fsocial.postservice.entity.SeenPost;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeenPostRepository extends MongoRepository<SeenPost, String> {

    @Query(value = "{'user_id': ?0}", fields = "{'post_id': 1, '_id': 0}")
    List<SeenPost> findByUserId(String userId);

    boolean existsByUserIdAndPostId(String userId, String postId);

    void deleteByUserId(String userId);
}
