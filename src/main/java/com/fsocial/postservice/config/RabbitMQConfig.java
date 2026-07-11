package com.fsocial.postservice.config;

import com.fsocial.postservice.enums.AMQPConst;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RabbitMQConfig {

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue postCommentDeleteQueue() {
        return new Queue(AMQPConst.POST_COMMENT_DELETE.getQueue(), true);
    }

    @Bean
    public Queue postCommentAttachmentsQueue() {
        return new Queue(AMQPConst.POST_ATTACHMENTS_DELETE.getQueue(), true);
    }

    @Bean
    public FanoutExchange postDeleteExchange(){
        return new FanoutExchange(AMQPConst.POST_COMMENT_DELETE.getExchange());
    }

    @Bean
    public Binding postCommentDeleteBinding(){
        return BindingBuilder.bind(postCommentDeleteQueue()).to(postDeleteExchange());
    }

    @Bean
    public Binding postCommentAttachmentsBinding(){
        return BindingBuilder.bind(postCommentAttachmentsQueue()).to(postDeleteExchange());
    }

    // --- Interaction events (BRD Feed Recommendation) ---

    @Bean
    public Queue scoreUpdateQueue() {
        return new Queue(AMQPConst.SCORE_UPDATE.getQueue(), true);
    }

    @Bean
    public Queue interestUpdateQueue() {
        return new Queue(AMQPConst.INTEREST_UPDATE.getQueue(), true);
    }

    @Bean
    public FanoutExchange interactionFanoutExchange() {
        return new FanoutExchange(AMQPConst.SCORE_UPDATE.getExchange());
    }

    @Bean
    public Binding scoreUpdateBinding() {
        return BindingBuilder.bind(scoreUpdateQueue()).to(interactionFanoutExchange());
    }

    @Bean
    public Binding interestUpdateBinding() {
        return BindingBuilder.bind(interestUpdateQueue()).to(interactionFanoutExchange());
    }

    // --- Notification events ---

    @Bean
    public Queue createNotificationQueue() {
        return new Queue(AMQPConst.CREATE_NOTIFICATION.getRoutingKey(), true);
    }

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(AMQPConst.CREATE_NOTIFICATION.getExchange());
    }

    @Bean
    public Binding createNotificationBinding() {
        return BindingBuilder.bind(createNotificationQueue())
                .to(notificationExchange())
                .with(AMQPConst.CREATE_NOTIFICATION.getRoutingKey());
    }
}
