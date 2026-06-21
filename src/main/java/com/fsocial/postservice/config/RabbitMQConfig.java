package com.fsocial.postservice.config;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RabbitMQConfig {
    @Value("${rabbitmq.queue.post.comment.delete}")
    String queueCommentDelete;

    @Value("${rabbitmq.queue.post.attachments.delete}")
    String queueCommentAttachments;

    @Value("${rabbitmq.exchange.post.delete}")
    String exchangePostDelete;

    // Interaction events for feed recommendation (BRD)
    @Value("${rabbitmq.queue.score.update}")
    String queueScoreUpdate;

    @Value("${rabbitmq.queue.interest.update}")
    String queueInterestUpdate;

    @Value("${rabbitmq.exchange.interaction.fanout}")
    String exchangeInteractionFanout;

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue postCommentDeleteQueue() {
        return new Queue(queueCommentDelete, true);
    }

    @Bean
    public Queue postCommentAttachmentsQueue() {
        return new Queue(queueCommentAttachments, true);
    }

    @Bean
    public FanoutExchange postDeleteExchange(){
        return new FanoutExchange(exchangePostDelete);
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
        return new Queue(queueScoreUpdate, true);
    }

    @Bean
    public Queue interestUpdateQueue() {
        return new Queue(queueInterestUpdate, true);
    }

    @Bean
    public FanoutExchange interactionFanoutExchange() {
        return new FanoutExchange(exchangeInteractionFanout);
    }

    @Bean
    public Binding scoreUpdateBinding() {
        return BindingBuilder.bind(scoreUpdateQueue()).to(interactionFanoutExchange());
    }

    @Bean
    public Binding interestUpdateBinding() {
        return BindingBuilder.bind(interestUpdateQueue()).to(interactionFanoutExchange());
    }
}
