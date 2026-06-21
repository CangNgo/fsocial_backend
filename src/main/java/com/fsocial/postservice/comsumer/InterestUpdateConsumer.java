package com.fsocial.postservice.comsumer;

import com.fsocial.postservice.dto.feed.InteractionEvent;
import com.fsocial.postservice.services.InterestGraphService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Consumes interaction events and updates user_interests weights.
 * Delta weights per BRD:
 *   LIKE   +2.0
 *   UNLIKE -2.0
 *   COMMENT +3.0
 *   SHARE  +5.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InterestUpdateConsumer {

    private final InterestGraphService interestGraphService;

    @RabbitListener(queues = "${rabbitmq.queue.interest.update}")
    public void handleInteractionEvent(InteractionEvent event) {
        if (event == null || event.getUserId() == null) return;

        List<String> tags = event.getPostTags();
        if (tags == null || tags.isEmpty()) return;

        double delta = resolveDelta(event.getActionType());
        if (delta == 0) return;

        interestGraphService.updateInterests(event.getUserId(), tags, delta);
        log.debug("Interest updated for user={} tags={} action={} delta={}",
                event.getUserId(), tags, event.getActionType(), delta);
    }

    private double resolveDelta(String actionType) {
        if (actionType == null) return 0;
        return switch (actionType) {
            case "LIKE"    -> 2.0;
            case "UNLIKE"  -> -2.0;
            case "COMMENT" -> 3.0;
            case "SHARE"   -> 5.0;
            default        -> 0.0;
        };
    }
}
