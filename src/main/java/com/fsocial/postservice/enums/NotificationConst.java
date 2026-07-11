package com.fsocial.postservice.enums;

import lombok.Getter;

@Getter
public enum NotificationConst {

    LIKE_SINGLE(
            NotificationType.LIKE_SINGLE,
            "Bài viết được yêu thích",
            "%s đã thích bài viết của bạn."
    ),

    LIKE_MULTI(
            NotificationType.LIKE_MULTI,
            "Bài viết được yêu thích",
            "%s và %d người khác đã thích bài viết của bạn."
    ),

    COMMENT_SINGLE(
            NotificationType.COMMENT_SINGLE,
            "Bình luận mới",
            "%s đã bình luận về bài viết của bạn."
    ),

    COMMENT_MULTI(
            NotificationType.COMMENT_MULTI,
            "Nhiều bình luận mới",
            "%s và %d người khác đã bình luận về bài viết của bạn."
    ),

    COMMENT_REPLY(
            NotificationType.COMMENT_REPLY,
            "Phản hồi bình luận",
            "%s đã trả lời bình luận của bạn."
    ),

    SHARE(
            NotificationType.SHARE,
            "Bài viết được chia sẻ",
            "%s đã chia sẻ bài viết của bạn."
    ),

    FOLLOW(
            NotificationType.FOLLOW,
            "Người theo dõi mới",
            "%s đã bắt đầu theo dõi bạn."
    ),

    MENTION(
            NotificationType.MENTION,
            "Bạn được nhắc đến",
            "%s đã nhắc đến bạn trong một bài viết hoặc bình luận."
    ),

    MESSAGE(
            NotificationType.MESSAGE,
            "Tin nhắn mới",
            "%s đã gửi cho bạn một tin nhắn."
    ),

    SYSTEM(
            NotificationType.SYSTEM,
            "Thông báo hệ thống",
            "%s"
    ),

    LOGIN(
            NotificationType.LOGIN,
            "Đăng nhập thành công",
            "Bạn đã đăng nhập thành công lúc %s."
    ),

    LOGIN_NEW_DEVICE(
            NotificationType.LOGIN_NEW_DEVICE,
            "Phát hiện đăng nhập mới",
            "Tài khoản của bạn vừa được đăng nhập trên thiết bị %s."
    ),

    REPORT(
            NotificationType.REPORT,
            "Báo cáo bài viết",
            "Bạn đã báo cáo bài viết thành công"
    );

    private final NotificationType type;
    private final String title;
    private final String body;

    NotificationConst(NotificationType type, String title, String body) {
        this.type = type;
        this.title = title;
        this.body = body;
    }

    public static NotificationConst from(NotificationType type) {
        return NotificationConst.valueOf(type.name());
    }
}