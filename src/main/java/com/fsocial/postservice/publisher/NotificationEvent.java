package com.fsocial.postservice.publisher;

import com.fsocial.postservice.dto.notification.NotificationDTO;
import com.fsocial.postservice.enums.AMQPConst;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationEvent {

    RabbitTemplate rabbitTemplate;

    public void publishCreateNotification(NotificationDTO notiDTO) {
        rabbitTemplate.convertAndSend(AMQPConst.CREATE_NOTIFICATION.getExchange(), AMQPConst.CREATE_NOTIFICATION.getRoutingKey(), notiDTO);
    }
}
