package com.fsocial.comsumer;

import com.fsocial.services.CommentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Component
@RequiredArgsConstructor
public class CommentConsumer {

    CommentService commentService;

    @RabbitListener(queues = "#{@postCommentDeleteQueue.name}")
    public void receiveComment(String postId) {
        commentService.deleteCommentByPostId(postId);
    }
}
