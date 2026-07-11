package com.fsocial.postservice.comsumer;

import com.fsocial.postservice.dto.feed.InteractionEvent;
import com.fsocial.postservice.entity.Post;
import com.fsocial.postservice.repository.CommentRepository;
import com.fsocial.postservice.repository.PostRepository;
import com.fsocial.postservice.services.ScoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScoreUpdateConsumer {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ScoringService scoringService;
    private final MongoTemplate mongoTemplate;

    @RabbitListener(queues = "#{@scoreUpdateQueue.name}")
    public void handleInteractionEvent(InteractionEvent event) {
        if (event == null || event.getPostId() == null) return;

        String postId = event.getPostId();

        // Handle share_count increment
        if ("SHARE".equals(event.getActionType())) {
            Query query = new Query(Criteria.where("_id").is(postId));
            mongoTemplate.updateFirst(query, new Update().inc("share_count", 1), Post.class);
        }

        postRepository.findById(postId).ifPresent(post -> {
            int commentCount = commentRepository.countByPostId(postId) == null ? 0
                    : commentRepository.countByPostId(postId);
            double rawEngagement = scoringService.calculateRawEngagement(post, commentCount);
            double newScore = scoringService.calculateGlobalScore(post, commentCount);

            Query query = new Query(Criteria.where("_id").is(postId));
            Update update = new Update()
                    .set("global_score", newScore)
                    .set("raw_engagement", rawEngagement);
            mongoTemplate.updateFirst(query, update, Post.class);

            log.debug("Updated global_score={} raw_engagement={} for post {}", newScore, rawEngagement, postId);
        });
    }
}
