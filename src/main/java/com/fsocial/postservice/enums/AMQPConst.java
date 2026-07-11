package com.fsocial.postservice.enums;

import lombok.Getter;

@Getter
public enum AMQPConst {
    POST_COMMENT_DELETE("post.delete.exchange", "post.comment.delete.queue", ""),
    POST_ATTACHMENTS_DELETE("post.delete.exchange", "post.attachments.delete.queue", ""),
    SCORE_UPDATE("interaction.fanout.exchange", "score.update.queue", ""),
    INTEREST_UPDATE("interaction.fanout.exchange", "interest.update.queue", ""),
    CREATE_NOTIFICATION("notification", "notification.create", "notification.create"),
    ;

    final String exchange;
    final String queue;
    final String routingKey;

    AMQPConst(String exchange, String queue, String routingKey) {
        this.exchange = exchange;
        this.queue = queue;
        this.routingKey = routingKey;
    }
}
