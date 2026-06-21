package com.fsocial.postservice.publisher;

import com.fsocial.postservice.dto.feed.InteractionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InteractionEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.interaction.fanout}")
    private String interactionFanoutExchange;

    public void publish(String postId, String userId, String actionType, List<String> postTags) {
        try {
            InteractionEvent event = InteractionEvent.builder()
                    .postId(postId)
                    .userId(userId)
                    .actionType(actionType)
                    .postTags(postTags != null ? postTags : List.of())
                    .timestamp(LocalDateTime.now())
                    .build();
            rabbitTemplate.convertAndSend(interactionFanoutExchange, "", event);
            log.debug("Published interaction event: postId={}, userId={}, action={}", postId, userId, actionType);
        } catch (Exception e) {
            log.warn("Failed to publish interaction event for post {}: {}", postId, e.getMessage());
        }
    }
}
