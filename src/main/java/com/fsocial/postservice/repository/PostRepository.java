package com.fsocial.postservice.repository;

import com.fsocial.postservice.dto.post.PostStatisticsDTO;
import com.fsocial.postservice.dto.post.PostStatisticsLongDateDTO;
import com.fsocial.postservice.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {

    @Aggregation(pipeline = {
            "{$match: {_id: ?0}}",
            "{ $project: {countLikes : {$size: '$likes'}}}"
    })
    Integer countLikeByPost(String postId);
    boolean existsByIdAndLikes(String postId, String userId);
    List<Post> findByOwnerUserId(String userId);

    List<Post> findByContentTextContainingIgnoreCase(String content);
    List<Post> findByIdNotInOrderByCreateDatetimeDesc(List<String> postIdViewed, Pageable pageable);

    @Query(value = "{ 'owner.user_id': { $in: ?0 }, '_id': { $nin: ?1 } }", sort = "{ 'created_datetime': -1 }")
    List<Post> findByOwnerUserIdInAndIdNotInOrderByCreateDatetimeDesc(List<String> userIds, List<String> postIds, Pageable pageable);

    // Feed recommendation queries (BRD)
    @Query(value = "{ 'tags': ?0, '_id': { $nin: ?1 }, 'status': true }", sort = "{ 'global_score': -1 }")
    List<Post> findByTagAndIdNotIn(String tag, List<String> excludedIds, Pageable pageable);

    @Query(value = "{ 'tags': { $in: ?0 }, '_id': { $nin: ?1 }, 'status': true }", sort = "{ 'global_score': -1 }")
    List<Post> findByTagsInAndIdNotIn(List<String> tags, List<String> excludedIds, Pageable pageable);

    @Query(value = "{ '_id': { $nin: ?0 }, 'status': true }", sort = "{ 'global_score': -1 }")
    List<Post> findTopByGlobalScore(List<String> excludedIds, Pageable pageable);

    @Aggregation(pipeline = {
            "{ '$match': { 'created_datetime': { '$gte': ?0, '$lte': ?1 } } }",
            "{ '$group': { '_id': { '$hour': '$created_datetime' }, 'count': { '$sum': 1 } } }",
            "{ '$project': { 'hour': '$_id', 'count': 1, '_id': 0 } }"
    })
    List<PostStatisticsDTO> countByCreatedAtByHours(LocalDateTime startDate, LocalDateTime endDate);

    @Aggregation(pipeline = {
            "{ '$match': { 'created_datetime': { '$gte': ?0, '$lte': ?1 } } }",
            "{ '$group': { '_id': { '$dateTrunc': { 'date': '$created_datetime', 'unit': 'day' } }, 'count': { '$sum': 1 } } }",
            "{ '$project': { 'date': '$_id', 'count': 1, '_id': 0 } }",
            "{ '$sort': { 'date': 1 } }"
    })
    List<PostStatisticsLongDateDTO> countByDate(LocalDateTime startDate, LocalDateTime endDate);
}
