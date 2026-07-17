package com.fsocial.postservice.repository;

import com.fsocial.postservice.entity.Comment;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {
    List<Comment> findByPostId(String postId);
    Integer countByPostId(String postId);

    @Aggregation(pipeline = {
            "{ '$match': { 'postId': { '$in': ?0 } } }",
            "{ '$group': { '_id': '$postId', 'count': { '$sum': 1 } } }"
    })
    List<PostCommentCount> countByPostIdIn(List<String> postIds);

    @Aggregation(pipeline = {
            "{'$match': {'_id': ?0}}",
            "{'$project': {'totalLikes': {'$size': '$likes'}}}"
    })
    Integer countLikes(String commentId);

    boolean existsByIdAndLikes(String id, String userId);

    boolean existsById(String id);

    Optional<Comment> findByRepliesId(String replyId);

    @Query(value = "{ '_id': ?0 }", fields = "{ 'postId': 1 }")
    Optional<String> findPostIdById(String commentId);

    record PostCommentCount(String _id, Integer count) {}
}
