package com.fsocial.postservice.publisher;

import com.fsocial.postservice.enums.AMQPConst;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostEventPublisher {

    RabbitTemplate rabbitTemplate;

    //Delete post
    public void eventDeletePost(String postId){
        rabbitTemplate.convertAndSend(AMQPConst.POST_COMMENT_DELETE.getExchange(), "", postId);
    }
}
