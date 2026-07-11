package com.fsocial.postservice.enums;

import lombok.Getter;

@Getter
public enum NotificationType {
    LIKE_SINGLE,
    LIKE_MULTI,
    COMMENT_SINGLE,
    COMMENT_MULTI,
    COMMENT_REPLY,
    SHARE,
    FOLLOW,
    MENTION,
    MESSAGE,
    SYSTEM,
    LOGIN,
    LOGIN_NEW_DEVICE
}